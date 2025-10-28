package com.fongmi.android.tv;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.databinding.DialogUpdateBinding;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Github;
import com.github.catvod.utils.Logger;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class Updater implements Download.Callback {

    private DialogUpdateBinding binding;
    private final Download download;
    private AlertDialog dialog;
    private boolean dev;
    private boolean forceCheck; // 是否为手动检查
    private String latestVersion; // 存储检测到的最新版本
    private String releaseApkUrl; // 从 GitHub Release 获取的 APK 下载链接

    private File getFile() {
        return Path.root("Download", "XMBOX-update.apk");
    }

    private String getJson() {
        return Github.getJson(dev, BuildConfig.FLAVOR_mode);
    }

    private String getApk() {
        // 优先使用从 GitHub Release 获取的 APK URL
        if (releaseApkUrl != null && !releaseApkUrl.isEmpty()) {
            Logger.d("APK download URL from Release: " + releaseApkUrl);
            return releaseApkUrl;
        }
        
        // 使用JSON中指定的具体下载路径
        try {
            String response = OkHttp.string(getJson());
            JSONObject object = new JSONObject(response);
            JSONObject downloads = object.optJSONObject("downloads");
            if (downloads != null) {
                String abi = BuildConfig.FLAVOR_abi;
                String downloadPath = downloads.optString(abi);
                if (!downloadPath.isEmpty()) {
                    // 直接构建完整URL，不通过Github.getApk()避免重复添加路径
                    String baseUrl = Github.useCnMirror() ? 
                        "https://gitee.com/ochenoktochen/XMBOX-Release/raw/main" :
                        "https://raw.githubusercontent.com/Tosencen/XMBOX-Release/main";
                    String fullUrl = baseUrl + "/apk/" + (dev ? "dev" : "release") + "/" + downloadPath;
                    Logger.d("APK download URL: " + fullUrl);
                    return fullUrl;
                }
            }
        } catch (Exception e) {
            Logger.e("Failed to get download path from JSON: " + e.getMessage());
        }
        // 回退到原来的方式
        String fallbackUrl = Github.getApk(dev, BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_abi);
        Logger.d("APK fallback URL: " + fallbackUrl);
        return fallbackUrl;
    }

    public static Updater create() {
        return new Updater();
    }

    public Updater() {
        this.download = Download.create(getApk(), getFile(), this);
        this.forceCheck = false;
    }

    public Updater force() {
        Notify.show(R.string.update_check);
        Setting.putUpdate(true);
        this.forceCheck = true; // 标记为手动检查
        return this;
    }

    public Updater release() {
        this.dev = false;
        return this;
    }

    public Updater dev() {
        this.dev = true;
        return this;
    }

    private Updater check() {
        dismiss();
        return this;
    }

    public void start(Activity activity) {
        App.execute(() -> doInBackground(activity));
    }

    private boolean need(int code, String name) {
        return Setting.getUpdate() && (dev ? !name.equals(BuildConfig.VERSION_NAME) && code >= BuildConfig.VERSION_CODE : code > BuildConfig.VERSION_CODE);
    }

    private void doInBackground(Activity activity) {
        Logger.d("Updater: Starting update check...");
        try {
            // 优先使用 JSON 方式检测更新（兼容性更好）
            String response = OkHttp.string(getJson());
            JSONObject object = new JSONObject(response);
            String name = object.optString("name");
            String desc = object.optString("desc");
            int code = object.optInt("code");
            
            Logger.d("Updater: JSON Remote version: " + name + ", code: " + code);
            Logger.d("Updater: Local version: " + BuildConfig.VERSION_NAME + ", code: " + BuildConfig.VERSION_CODE);
            
            // 使用 JSON 中的版本信息
            if (need(code, name)) {
                Logger.d("Updater: Update needed (from JSON), showing dialog");
                this.latestVersion = name; // 保存最新版本号
                
                // 从 JSON 获取下载链接
                JSONObject downloads = object.optJSONObject("downloads");
                if (downloads != null) {
                    String abi = BuildConfig.FLAVOR_abi;
                    String downloadPath = downloads.optString(abi);
                    if (!downloadPath.isEmpty()) {
                        String baseUrl = Github.useCnMirror() ? 
                            "https://gitee.com/ochenoktochen/XMBOX-Release/raw/main" :
                            "https://raw.githubusercontent.com/Tosencen/XMBOX-Release/main";
                        this.releaseApkUrl = baseUrl + "/apk/" + (dev ? "dev" : "release") + "/" + downloadPath;
                        Logger.d("Updater: APK URL from JSON: " + this.releaseApkUrl);
                    }
                }
                
                App.post(() -> show(activity, name, desc));
            } else {
                Logger.d("Updater: No update needed (from JSON)");
                if (forceCheck) {
                    App.post(() -> Notify.show("已是最新版本 " + name));
                }
            }
        } catch (Exception e) {
            Logger.e("Updater: JSON check failed, trying GitHub API: " + e.getMessage());
            // JSON 检测失败，尝试使用 GitHub Releases API
            checkViaGitHubAPI(activity);
        }
    }
    
    private void checkViaGitHubAPI(Activity activity) {
        try {
            String releasesUrl = "https://api.github.com/repos/Tosencen/XMBOX/releases/latest";
            Logger.d("Updater: Trying GitHub Releases API: " + releasesUrl);
            
            String response = OkHttp.string(releasesUrl);
            
            if (response.contains("rate limit exceeded")) {
                Logger.e("Updater: Rate limit exceeded");
                if (forceCheck) {
                    App.post(() -> Notify.show("检查更新失败：API请求过于频繁，请稍后重试"));
                }
                return;
            }
            
            if (response.contains("Not Found") || response.contains("404")) {
                Logger.e("Updater: Release not found");
                if (forceCheck) {
                    App.post(() -> Notify.show("检查更新失败：无法连接到更新服务器"));
                }
                return;
            }
            
            JSONObject release = new JSONObject(response);
            String tagName = release.optString("tag_name");
            String body = release.optString("body");
            String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            
            Logger.d("Updater: GitHub API Remote version: " + version);
            
            // 从 assets 中查找 APK
            JSONArray assets = release.optJSONArray("assets");
            if (assets != null) {
                String targetApkName = BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_abi + "-v" + version + ".apk";
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    if (targetApkName.equals(asset.optString("name"))) {
                        this.releaseApkUrl = asset.optString("browser_download_url");
                        break;
                    }
                }
            }
            
            if (needUpdate(version)) {
                this.latestVersion = version;
                App.post(() -> show(activity, version, body));
            } else if (forceCheck) {
                App.post(() -> Notify.show("已是最新版本 " + version));
            }
        } catch (Exception e) {
            Logger.e("Updater: GitHub API check failed: " + e.getMessage());
            if (forceCheck) {
                App.post(() -> Notify.show("检查更新失败：无法连接到更新服务器"));
            }
        }
    }
    
    private boolean needUpdate(String remoteVersion) {
        if (!Setting.getUpdate()) return false;
        
        try {
            // 简单的版本号比较，假设版本格式为 x.y.z
            String[] remoteParts = remoteVersion.split("\\.");
            String[] localParts = BuildConfig.VERSION_NAME.split("\\.");
            
            // 确保两个版本号都有足够的段
            int maxLength = Math.max(remoteParts.length, localParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;
                int localPart = i < localParts.length ? Integer.parseInt(localParts[i]) : 0;
                
                if (remotePart > localPart) {
                    return true;
                } else if (remotePart < localPart) {
                    return false;
                }
            }
            return false; // 版本相同
        } catch (Exception e) {
            Logger.e("Updater: Version comparison error: " + e.getMessage());
            return false;
        }
    }

    private void show(Activity activity, String version, String desc) {
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(activity));
        check().create(activity, ResUtil.getString(R.string.update_version, version)).show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this::confirm);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this::cancel);
        binding.desc.setText(desc);
    }

    private AlertDialog create(Activity activity, String title) {
        return dialog = new MaterialAlertDialogBuilder(activity).setTitle(title).setView(binding.getRoot()).setPositiveButton(R.string.update_confirm, null).setNegativeButton(R.string.dialog_negative, null).setCancelable(false).create();
    }

    private void cancel(View view) {
        Setting.putUpdate(false);
        download.cancel();
        dialog.dismiss();
    }

    private void confirm(View view) {
        // 跳转到具体版本的GitHub Releases页面
        try {
            String url = "https://github.com/Tosencen/XMBOX/releases/tag/v" + latestVersion;
            Logger.d("Updater: Attempting to open URL: " + url);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(App.get().getPackageManager()) != null) {
                App.get().startActivity(intent);
                Logger.d("Updater: Successfully started browser intent");
                dismiss();
            } else {
                Logger.e("Updater: No app can handle the URL");
                Notify.show("没有找到可以打开链接的应用，请手动访问GitHub下载");
                dismiss();
            }
        } catch (Exception e) {
            Logger.e("Updater: Failed to open GitHub releases page: " + e.getMessage());
            e.printStackTrace();
            Notify.show("无法打开更新页面，请手动访问GitHub下载");
            dismiss();
        }
    }

    private void dismiss() {
        try {
            if (dialog != null) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void progress(int progress) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(String.format(Locale.getDefault(), "%1$d%%", progress));
    }

    @Override
    public void error(String msg) {
        Notify.show(msg);
        dismiss();
    }

    @Override
    public void success(File file) {
        FileUtil.openFile(file);
        dismiss();
    }
}