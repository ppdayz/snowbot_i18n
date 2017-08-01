package com.csjbot.snowbot.bean.aiui;

import android.text.TextUtils;

import com.csjbot.snowbot.bean.aiui.entity.CMDResult;
import com.csjbot.snowbot.bean.aiui.entity.InfoResult;
import com.csjbot.snowbot.bean.aiui.entity.MusicResult;
import com.csjbot.snowbot.bean.aiui.entity.OpenQAResult;
import com.csjbot.snowbot.bean.aiui.entity.RobotActionResult;
import com.csjbot.snowbot.bean.aiui.entity.SemanticResult;
import com.csjbot.snowbot.bean.aiui.entity.SmartHomeResult;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 语义结果解析类，将json格式的语义结果解析为相应的实体类。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:48:32
 */
public class SemanticResultParser {

    private final static String KEY_SERVICE = "service";

    // 解析语义结果，无service字段或者service非演示的业务则返回null
    public static SemanticResult parse(JSONObject jsonObject) {
        SemanticResult semanticResult = null;
        try {
            // 增加判空操作，因为jsonObject有可能为null
            if (null == jsonObject) {
                return null;
            }
            //拒识灯控制
//			DevBoardControlUtil.rejectionLight(jsonObject);

            if (!jsonObject.has(KEY_SERVICE)) {
                return null;
            }

            String service = jsonObject.getString(KEY_SERVICE);

            if (!TextUtils.isEmpty(service)) {
                SemanticResult.ServiceType serviceType = SemanticResult.getServiceType(service);

                switch (serviceType) {
                    case WEATHER:
                    case TRAIN:
                    case FLIGHT:
                    case TVCHANNEL:
                    case EPG:
                    case COOKBOOK:
                    case CALC:
                    case PM25:
                    case DATETIME:
                    case TELEPHONE:
                    case RADIO:
                    case NUMBER_MASTER:  //数字大师
                    case NEWS:
                    case STORY:
                    case PATTERN:
                    case CHAT:
                    case POETRY:
                    case JOKE:
                        semanticResult = new InfoResult(service, jsonObject);
                        break;
                    case MUSICX:
                        semanticResult = new MusicResult(service, jsonObject);
                        break;
                    case TV_SMARTH:
                        break;
                    case SMARTHOME:
                        semanticResult = new SmartHomeResult(service, jsonObject);
                        break;
                    case CMD:
                        semanticResult = new CMDResult(service, jsonObject);
                        break;
                    case ROBOTACTION:
                        semanticResult = new RobotActionResult(service, jsonObject);
                        break;
                    case OPENQA:
                        semanticResult = new OpenQAResult(service, jsonObject);
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return semanticResult;
    }

    public static SemanticResult parse(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return parse(jsonObject);
    }

}
