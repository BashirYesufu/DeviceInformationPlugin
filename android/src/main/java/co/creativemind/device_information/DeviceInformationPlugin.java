package co.creativemind.device_information;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/** DeviceInformationPlugin */
public class DeviceInformationPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
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
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
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
          result.success(Build.CPU_ABI); // Deprecated, but safe fallback
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
    }

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return telephonyManager != null ? telephonyManager.getImei() : "";
      } else {
        return telephonyManager != null ? telephonyManager.getDeviceId() : "";
      }
    } catch (Exception e) {
      return "";
    }
  }

  // ActivityAware methods
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
