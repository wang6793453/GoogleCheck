package google;


import com.google.gson.Gson;
import google.data.AuthData;
import google.data.ConfigData;

import java.util.HashMap;

class GooglePay {


    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    public static void main(String[] args) {

        //官方文档：https://developers.google.com/android-publisher/authorization
        //按照文档配置完后，获取到json格式的配置文件。

        //第一步 将json文件中的内容，粘贴到local.properties文件中
        String jsonFileString = Util.getProperty("json_file_value");
        ConfigData.InstalledBean installedBean = new Gson().fromJson(jsonFileString, ConfigData.class).getInstalled();
        if (installedBean == null) {
            System.out.println("请json文件中的内容，粘贴到local.properties文件中\n");
            return;
        }

        //第二步，拼接出AuthUrl
        String getAuthUrl = getRefreshTokenUrl(installedBean.getRedirect_uris().get(0), installedBean.getClient_id());
        System.out.println(String.format("the authUrl is: %s\n", getAuthUrl));

        //第三步，复制AuthUrl，粘贴到浏览器中进行访问，进入界面后会有授权的操作，操作成功之后界面会显示一个code，粘贴到local.properties中
        String code = Util.getProperty("code");
        if (code == null || code.equals("")) {
            System.out.println("请粘贴code到local.properties中");
            return;
        }

        //第四步，获取refreshToken，未来需要使用这个refreshToken去刷新accessToken。
        // accessToken才是最终去验证订单的凭证，但是有有效期，过期了就需要使用refreshToken刷新。
        //注意：一个code只能获取一次refreshToken，重新访问接口会报错，所以务必保存好refreshToken，万一忘了或需要修改，从第二步重新开始。
        String refreshToken = Util.getProperty("refreshToken");
        if (refreshToken == null || refreshToken.equals("")) {
            refreshToken = getRefreshToken(installedBean, code);
            System.out.printf("the refreshToken is:%s \n", refreshToken);
        }

        //第五步，使用refreshToken获取accessToken
        String accessToken = getAccessToken(installedBean, refreshToken);
        System.out.printf("the accessToken is:%s \n", accessToken);

        //最后一步：使用accessToken去请求接口，purchaseToken是每一笔订单支付成功后返回的。
        System.out.println("检查测试数据。");
        System.out.println(HttpUtil.sendGet(getCheckUrl(accessToken, "android.test.purchased", "inapp:com.picaqiu.bikabika:android.test.purchased")));
        System.out.println("检查假数据");
        System.out.println(HttpUtil.sendGet(getCheckUrl(accessToken, "gold_49800", "yfzfmvmbrjahmafdrjlvbiwm.AO-J1OFNmwvdzPDEeWQjAOLytyvERLkYhZmgdRMJYSmxVgB-ZcTWfXG_yJvUZdwJVwstumvKMnNMVoAWWFCtVgAshUJrxfiFSiqREKRmBgNejyGVcOpbMwcPGAUdqyfTnmWUxQDdLkxc")));

    }

    private static String getAccessToken(ConfigData.InstalledBean installedBean, String refreshToken) {
        HashMap<String, String> param = new HashMap<>();
        param.put("grant_type", GRANT_TYPE_REFRESH_TOKEN);
        param.put("client_id", installedBean.getClient_id());
        param.put("client_secret", installedBean.getClient_secret());
        param.put("refresh_token", refreshToken);
        String authResult = HttpUtil.sendPost(AUTH_URL, param);

        AuthData authBean = new Gson().fromJson(authResult, AuthData.class);
        return authBean.getAccess_token();
    }

    private static String getRefreshToken(ConfigData.InstalledBean installedBean, String c4) {
        HashMap<String, String> param = new HashMap<>();
        param.put("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);
        param.put("code", c4);//刚刚获取的C4
        param.put("client_id", installedBean.getClient_id());
        param.put("client_secret", installedBean.getClient_secret());
        param.put("redirect_uri", installedBean.getRedirect_uris().get(0));
        String authResult = HttpUtil.sendPost(AUTH_URL, param);
        AuthData authBean = new Gson().fromJson(authResult, AuthData.class);
        return authBean.getRefresh_token();
    }

    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";


    /**
     * 根据订单信息检测订单是否支付成功，acknowledgementState=1表示支付成功
     */
    public static String getCheckUrl(String accessToken, String productId, String purchaseToken) {
        String url = "https://www.googleapis.com/androidpublisher/v3/applications/" + Util.getProperty("package_name");
        return String.format("%s/purchases/products/%s/tokens/%s?access_token=%s", url, productId, purchaseToken, accessToken);
    }

    /**
     * 根据Auth客户端配置获取refhreshToken
     */
    private static String getRefreshTokenUrl(String redirectUrl, String clientId) {
        return String.format("https://accounts.google.com/o/oauth2/auth?scope=https://www.googleapis.com/auth/androidpublisher&response_type=code&access_type=offline&redirect_uri=%s&client_id=%s", redirectUrl, clientId);
    }


}

