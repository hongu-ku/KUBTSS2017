# KUBTSS2017

##　 設計の目的と背景
このアプリは2017年の鳥人間コンテストでの飛行時でパイロットに必要な情報(琵琶湖のマップ、各センサーの値、姿勢計)を見せるため、さらに
TF時のログを残すために作られた。

## 設計条件

- タブレットはdocomoから出ていた、Huawei製のdtabである。バージョン等は忘れた。
- タブレットはフェアリングに固定。
- 各センサーの値はメイン基板からBluetoothで送信される。
- MapはGoogleAPIを用いる。
- GPSと姿勢計に関してはAndroidの内蔵センサーを用いる
