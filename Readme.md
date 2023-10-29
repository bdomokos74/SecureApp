### Prepare cert files for the tests

```
mkdir -p ssl/server ssl/client

cd ssl/server
keytool -genkeypair -dname "CN=localhost, OU=Java, O=Home, L=ZH, S=ZH, C=CH" -alias server -keyalg rsa -keystore keystore.p12 -validity 180
keytool -keystore keystore.p12 -rfc -exportcert -alias server > svr.cer

cd ../client
keytool -genkeypair -alias bds -dname "CN=bds, OU=Java, O=Home, L=ZH, S=ZH, C=CH" -keyalg rsa -keystore clistore.p12 -validity 180
keytool -import -keystore .\truststore.p12 -file ..\server\svr.cer -alias server
keytool -keystore clistore.p12 -rfc -exportcert -alias bds > cli.cer

keytool -genkeypair -alias other -dname "CN=otherclient, OU=Java, O=Home, L=ZH, S=ZH, C=CH" -keyalg rsa -keystore clistore2.p12 -validity 180
keytool -keystore clistore2.p12 -rfc -exportcert -alias other > othercli.cer

keytool -genkeypair -alias invalid -dname "CN=invalidclient, OU=Java, O=Home, L=ZH, S=ZH, C=CH" -keyalg rsa -keystore clistore_invalid.p12 -validity 180

cd ../server
keytool -import -keystore .\truststore.p12 -file ..\client\cli.cer -alias bds

keytool -import -keystore .\truststore_both.p12 -file ..\client\cli.cer -alias bds
keytool -import -keystore .\truststore_both.p12 -file ..\client\othercli.cer -alias other

# To clean up:
cd ssl/server
rm *
cd ../client 
rm *

```
