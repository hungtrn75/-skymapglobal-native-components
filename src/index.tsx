import { NativeEventEmitter, NativeModules } from 'react-native';

interface Location {
  longitude: number | null | undefined;
  latitude: number | null | undefined;
  speed: number | null | undefined;
  altitude: number | null | undefined;
  accuracy: number | null | undefined;
  course: number | null | undefined;
}

interface SkymapModulesType {
  CBLocation: typeof CBLocation;
  CBNative: typeof CBNative;
}

type Callback = (location: Location) => void;

const {
  CBLocation: CBLocationModule,
  CBNative: CBNativeModule,
} = NativeModules;
const CBLocationEmitter = new NativeEventEmitter(CBLocationModule);

class CBLocation {
  static async getCurrentLocation() {
    return CBLocationModule.getCurrentLocation();
  }

  static startUpdatingLocation() {
    CBLocationModule.startUpdatingLocation();
  }

  static stopUpdatingLocation() {
    CBLocationModule.stopUpdatingLocation();
  }

  static openLocationSettings() {
    CBLocationModule.openLocationSettings();
  }

  static openAppPermissions() {
    CBLocationModule.openAppPermissions();
  }

  static onLocationChange(callback: Callback) {
    return CBLocationEmitter.addListener('CBLocationChange', callback);
  }
}

class CBNative {
  static show() {
    CBNativeModule.showLoading();
  }

  static hide() {
    CBNativeModule.hideLoading();
  }

  static deviceInfo() {
    return {
      appVersion: CBNativeModule.appVersion,
      buildVersion: CBNativeModule.buildVersion,
      imei: CBNativeModule.imei,
    };
  }
}

const SkymapModules: SkymapModulesType = { CBLocation, CBNative };

export default SkymapModules;
