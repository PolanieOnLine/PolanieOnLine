const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const root=path.resolve(__dirname,'../client');
const read=file=>fs.readFileSync(path.join(root,file),'utf8');
test('Google Play build disables the website updater and removes the installer surfaces',()=>{
    const gradle=read('build.gradle');
    assert.match(gradle,/playRelease\s*\{[\s\S]*?DIRECT_UPDATES', 'false'/);
    const play=read('src/playRelease/AndroidManifest.xml');
    assert.match(play,/REQUEST_INSTALL_PACKAGES" tools:node="remove"/);
    assert.match(play,/FileProvider" tools:node="remove"/);
});
test('only verified private updater files are shared with the Android installer',()=>{
    const paths=read('src/main/res/xml/update_paths.xml');
    assert.match(paths,/<cache-path name="verified-update" path="android-updater\/verified\/"/);
    assert.doesNotMatch(paths,/<(?:root|external|files)-path/);
    const manifest=read('src/main/AndroidManifest.xml');
    assert.match(manifest,/UpdateActivity" android:exported="false"/);
    assert.match(manifest,/FileProvider[\s\S]*?android:exported="false"/);
});
