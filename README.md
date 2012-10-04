TCClick统计平台安卓客户端
===============

使用步骤：

1. 下载tcclick.jar，作为lib库放入到你的应用中，[点击下载tcclick.jar](https://github.com/starfalling/tcclick-android/raw/master/tcclick.jar)
2. 打开项目的Android.xml文件，在application层级加入tcclick服务器端的数据上传地址以及应用的渠道名，如：

        <application>
            ...
            <meta-data android:value="http://tcclicktest.sinaapp.com/api/upload.php" android:name="TCCLICK_API_UPLOAD" />
            <meta-data android:value="debug" android:name="TCCLICK_CHANNEL" />
        </application>
4. 重载你的Activity的onResume方法和onPause方法，可以编写一个重载了Activity的基类来做这件事情，然后让应用中所有的其他Activity都继承这个基类，如：

        public class BaseActivity extends Activity {
        
            public void onResume(){
                super.onResume();
                com.truecolor.tcclick.TCClick.onResume(this);
            }
            
            public void onPause(){
                super.onPause();
                com.truecolor.tcclick.TCClick.onPause(this);
            }
        }
5. 到此，客户端的所有工作都已经完成了。

除了在meta-data中配置TCCLICK_CHANNEL之外，tcclick还提供了另外一种方法对渠道代码进行配置，我们在TCClick类中提供了一个静态的setChannel函数，可以在Application启动的时候调用这个函数使用代码对渠道码进行设置