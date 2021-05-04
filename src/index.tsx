import { NativeModules } from 'react-native';

type NativeComponentsType = {
  multiply(a: number, b: number): Promise<number>;
};

const { NativeComponents } = NativeModules;

export default NativeComponents as NativeComponentsType;
