package co.creativemind.device_information;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaDrm;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
//import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** DeviceInformationPlugin */
public class DeviceInformationPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "device_information");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + Build.VERSION.RELEASE);
        break;

      case "getIMEINumber":
        String imeiNo = getIMEINo();
        if (Manifest.permission.READ_PHONE_STATE.equals(imeiNo)) {
          result.error("PERMISSION_DENIED", "Permission READ_PHONE_STATE is not granted!", null);
        } else if (imeiNo != null && imeiNo.length() > 0) {
          result.success(imeiNo);
        } else {
          result.error("UNAVAILABLE", "IMEI not available", null);
        }
        break;

      case "getAPILevel":
        result.success(Build.VERSION.SDK_INT);
        break;

      case "getModel":
        result.success(Build.MODEL);
        break;

      case "getManufacturer":
        result.success(Build.MANUFACTURER);
        break;

      case "getDevice":
        result.success(Build.DEVICE);
        break;

      case "getProduct":
        result.success(Build.PRODUCT);
        break;

      case "getCPUType":
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          result.success(Arrays.toString(Build.SUPPORTED_ABIS));
        } else {
          result.success(Build.CPU_ABI);
        }
        break;

      case "getHardware":
        result.success(Build.HARDWARE);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  @SuppressLint({"HardwareIds", "MissingPermission"})
  private String getIMEINo() {
    if (activity == null) return "";

    TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
      return Manifest.permission.READ_PHONE_STATE;
    } else {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        return getDeviceUniqueID();
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (telephonyManager != null && telephonyManager.getImei() != null) {
          return telephonyManager.getImei();
        }
      } else {
        if (telephonyManager != null && telephonyManager.getDeviceId() != null) {
          return telephonyManager.getDeviceId();
        }
      }
    }
    return "";
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  @Nullable
  private String getDeviceUniqueID() {
    UUID wideVineUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    try {
      MediaDrm wvDrm = new MediaDrm(wideVineUuid);
      byte[] wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
      String str = Arrays.toString(wideVineId)
              .replaceAll("\\[", "")
              .replaceAll("]", "")
              .replaceAll(",", "")
              .replaceAll("-", "")
              .replaceAll(" ", "");
      return str.length() > 15 ? str.substring(0, 15) : str;
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }
}
