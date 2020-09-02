package com.kha.loomoconnection.restserver.controller;

import com.kha.loomoconnection.loomocontrol.HeadModule;
import com.kha.loomoconnection.loomocontrol.LocomotionModule;
import com.kha.loomoconnection.loomocontrol.SensorModule;
import com.kha.loomoconnection.loomocontrol.VisionModule;
import com.kha.loomoconnection.loomocontrol.VoiceModule;
import com.kha.loomoconnection.restserver.model.data.HeadStatus;
import com.kha.loomoconnection.restserver.model.data.Point2D;
import com.kha.loomoconnection.restserver.model.data.Point2Dtheta;
import com.kha.loomoconnection.restserver.model.requests.AddCPRequest;
import com.kha.loomoconnection.restserver.model.requests.AddCPThetaRequest;
import com.kha.loomoconnection.restserver.model.requests.BaseRawMovementRequest;
import com.kha.loomoconnection.restserver.model.requests.BaseRequest;
import com.kha.loomoconnection.restserver.model.requests.BatchAddCPRequest;
import com.kha.loomoconnection.restserver.model.requests.BatchAddCPThetaRequest;
import com.kha.loomoconnection.restserver.model.requests.CPProgressRequest;
import com.kha.loomoconnection.restserver.model.requests.CleanCPRequest;
import com.kha.loomoconnection.restserver.model.requests.ClearMetersRequest;
import com.kha.loomoconnection.restserver.model.requests.EmojiRequest;
import com.kha.loomoconnection.restserver.model.requests.FollowRequest;
import com.kha.loomoconnection.restserver.model.requests.GetBaseInfoRequest;
import com.kha.loomoconnection.restserver.model.requests.GetMetersRequest;
import com.kha.loomoconnection.restserver.model.requests.GetOdometryByTimeRequest;
import com.kha.loomoconnection.restserver.model.requests.GetOdometryRequest;
import com.kha.loomoconnection.restserver.model.requests.HeadLightRequest;
import com.kha.loomoconnection.restserver.model.requests.HeadMovementRequest;
import com.kha.loomoconnection.restserver.model.requests.ImageRequest;
import com.kha.loomoconnection.restserver.model.requests.LocomotionModeChangeRequest;
import com.kha.loomoconnection.restserver.model.requests.MeetObstacleRequest;
import com.kha.loomoconnection.restserver.model.requests.ObstacleAvoidanceRequest;
import com.kha.loomoconnection.restserver.model.requests.ResetOriginRequest;
import com.kha.loomoconnection.restserver.model.requests.SensorDataRequest;
import com.kha.loomoconnection.restserver.model.requests.SetOriginRequest;
import com.kha.loomoconnection.restserver.model.requests.VoiceRequest;
import com.kha.loomoconnection.restserver.model.responses.BaseRsp;
import com.segway.robot.algo.Pose2D;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RestController;

import java.util.ArrayList;

@RestController
public class LoomoController {


    @PostMapping("/loomo/speak")
    BaseRsp speak(@RequestBody VoiceRequest requestData) {
        // 传输中文或英文文本，通过扬声器播放
        boolean SON = VoiceModule.getInstance().speak(requestData.getVoiceline());
        if (SON) {
            BaseRsp rsp = new BaseRsp();
            rsp.setId(requestData.getId());
            rsp.setInfo("Loomo speak success");
            return rsp;
        } else {
            BaseRsp errRsp = new BaseRsp();
            errRsp.setId(requestData.getId());
            errRsp.setInfo("Voice Module Error!");
            errRsp.setSuccess(false);
            return errRsp;
        }
    }

    @PostMapping("/loomo/headLight")
    BaseRsp headLight(@RequestBody HeadLightRequest requestData) {
        // 0为关闭侧灯，1-13为不同样式
        boolean SON = HeadModule.getInstance().setHeadLight(requestData.range);
        if (SON) {
            BaseRsp rsp = new BaseRsp();
            rsp.setId(requestData.getId());
            rsp.setInfo("Loomo set headlight success");
            return rsp;
        } else {
            BaseRsp errRsp = new BaseRsp();
            errRsp.setId(requestData.getId());
            errRsp.setInfo("Head Module Error!");
            errRsp.setSuccess(false);
            return errRsp;
        }
    }

    @PostMapping("/loomo/headInfo")
    BaseRsp fetchHeadInfo(@RequestBody BaseRequest requestData) {
        // 0为关闭侧灯，1-13为不同样式
        HeadStatus headInfo = HeadModule.getInstance().fetchHeadInfo();
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setInfo("Loomo fetch head info success");
        rsp.setData(headInfo);
        return rsp;
    }

    @PostMapping("/loomo/headMovement")
    BaseRsp setHeadMovement(@RequestBody HeadMovementRequest requestData) {
        boolean SON = HeadModule.getInstance().updateHeadMovement(requestData);
        if (SON) {
            BaseRsp rsp = new BaseRsp();
            rsp.setId(requestData.getId());
            rsp.setInfo("Loomo set head movement success");
            return rsp;
        } else {
            BaseRsp errRsp = new BaseRsp();
            errRsp.setId(requestData.getId());
            errRsp.setInfo("Head Module Error!");
            errRsp.setSuccess(false);
            return errRsp;
        }
    }

    @PostMapping("/loomo/emoji")
    BaseRsp setEmojiMode(@RequestBody EmojiRequest requestData) {
        //查看是否需要切换emoji模式
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        if (requestData.emojiMode != HeadModule.emojiMode) {
            HeadModule.getInstance().switchEmoji(requestData.emojiMode);
            rsp.setInfo("Mode switch complete, please place further input");
            rsp.setSuccess(true);
            return rsp;
        } else if (requestData.emojiMode) {
            HeadModule.getInstance().makeEmoji(requestData.behavior);
        }
        return rsp;
    }

    @PostMapping("/loomo/getImage")
    BaseRsp getImage(@RequestBody ImageRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        rsp.setData(VisionModule.getInstance().getImageData());
        return rsp;
    }

    @PostMapping("/loomo/baseMode")
    BaseRsp changeBaseMode(@RequestBody LocomotionModeChangeRequest requestData) {
        LocomotionModule.getInstance().switchMode(requestData.mode);
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/follow")
    BaseRsp follow(@RequestBody FollowRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            rsp.setSuccess(LocomotionModule.getInstance().startPersonTracking());
        } else {
            LocomotionModule.getInstance().stopTracking();
            rsp.setSuccess(true);
        }
        rsp.setId(requestData.getId());
        return rsp;
    }

    @PostMapping("/loomo/setOrigin")
    BaseRsp setOrigin(@RequestBody SetOriginRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        LocomotionModule.getInstance().setOrigin(new Pose2D(requestData.getX(),
                requestData.getY(), 0, 0, 0, 0));
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/resetOrigin")
    BaseRsp resetOrigin(@RequestBody ResetOriginRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            LocomotionModule.getInstance().resetOrigin();
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/getCPProgress")
    BaseRsp CPProgress(@RequestBody CPProgressRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            rsp.setData(LocomotionModule.getInstance().fetchCPProgress());
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/clearMeters")
    BaseRsp clearMeters(@RequestBody ClearMetersRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            LocomotionModule.getInstance().clearMeters();
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/getMeters")
    BaseRsp getMeters(@RequestBody GetMetersRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            rsp.setData(LocomotionModule.getInstance().getMeters());
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/getRoughMeters")
    BaseRsp getRoughMeters(@RequestBody GetMetersRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            rsp.setData(LocomotionModule.getInstance().getRoughMeters());
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/addCP")
    BaseRsp addCP(@RequestBody AddCPRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        Point2D point2D = new Point2D();
        point2D.setX(requestData.getX());
        point2D.setY(requestData.getY());
        LocomotionModule.getInstance().addCheckPoint2D(point2D);
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/addCPTheta")
    BaseRsp addCPTheta(@RequestBody AddCPThetaRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        Point2Dtheta point2Dtheta = new Point2Dtheta();
        point2Dtheta.setTheta(requestData.getTheta());
        point2Dtheta.setX(requestData.getX());
        point2Dtheta.setY(requestData.getY());
        LocomotionModule.getInstance().addCheckPoint2DWithTheta(point2Dtheta);
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/batchAddCP")
    BaseRsp batchAddCP(@RequestBody BatchAddCPRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        if (requestData.getX().length != requestData.getY().length) {
            rsp.setSuccess(false);
            return rsp;
        }
        ArrayList<Point2D> point2DArray = new ArrayList<>();
        for (int i = 0; i < requestData.getX().length; i++){
            float x = requestData.getX()[i];
            float y = requestData.getY()[i];
            Point2D point2D = new Point2D();
            point2D.setY(y);
            point2D.setX(x);
            point2DArray.add(point2D);
        }
        LocomotionModule.getInstance().batchAddCheckPoint2D(point2DArray);
        return rsp;
    }

    @PostMapping("/loomo/batchAddCPTheta")
    BaseRsp batchAddCPTheta(@RequestBody BatchAddCPThetaRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        if (requestData.getX().length != requestData.getY().length || requestData.getX().length != requestData.getTheta().length) {
            rsp.setSuccess(false);
            return rsp;
        }
        ArrayList<Point2Dtheta> point2DArray = new ArrayList<>();
        for (int i = 0; i < requestData.getX().length; i++){
            float x = requestData.getX()[i];
            float y = requestData.getY()[i];
            float theta = requestData.getTheta()[i];
            Point2Dtheta point2D = new Point2Dtheta();
            point2D.setY(y);
            point2D.setX(x);
            point2D.setTheta(theta);
            point2DArray.add(point2D);
        }
        LocomotionModule.getInstance().batchAddCheckPoint2DWithTheta(point2DArray);
        return rsp;
    }

    @PostMapping("/loomo/obstacleAvoidance")
    BaseRsp obstacleAvoidance(@RequestBody ObstacleAvoidanceRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        LocomotionModule.getInstance().toggleObstacleAvoidance(requestData.trigger, requestData.distance);
        return rsp;
    }

    @PostMapping("/loomo/meetObstacle")
    BaseRsp meetObstacle(@RequestBody MeetObstacleRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        if (requestData.trigger) {
            rsp.setData(LocomotionModule.getInstance().getmeetObstacle());
        }
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        return rsp;
    }

    @PostMapping("/loomo/cleanCP")
    BaseRsp cleanCP(@RequestBody CleanCPRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        if (requestData.trigger){
            LocomotionModule.getInstance().cleanCheckPoints();
        }
        return rsp;
    }

    @PostMapping("/loomo/getOdometry")
    BaseRsp getOdometry(@RequestBody GetOdometryRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        if (requestData.trigger){
            if (LocomotionModule.getInstance().VLSenabled) {
                rsp.setData(LocomotionModule.getInstance().getLatestOdometryByVLS());
            } else{
                rsp.setData(LocomotionModule.getInstance().getLatestOdometry());
            }
        }
        return rsp;
    }

    @PostMapping("/loomo/getOdometryByTime")
    BaseRsp getOdometryByTime(@RequestBody GetOdometryByTimeRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        if (requestData.trigger){
            if (LocomotionModule.getInstance().VLSenabled) {
                rsp.setData(LocomotionModule.getInstance().getOdometryByTimeByVLS(requestData.microseconds));
            } else{
                rsp.setData(LocomotionModule.getInstance().getOdometryByTime(requestData.microseconds));
            }
        }
        return rsp;
    }

    @PostMapping("/loomo/setBaseMovement")
    BaseRsp setBaseMovement(@RequestBody BaseRawMovementRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        LocomotionModule.getInstance().setBaseMovement(requestData);
        return rsp;
    }

    @PostMapping("/loomo/sensorData")
    BaseRsp fetchSensorData(@RequestBody SensorDataRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        if (requestData.trigger) {
            rsp.setData(SensorModule.getInstance().fetchSensorData());
        }
        return rsp;
    }

    @PostMapping("/loomo/baseInfo")
    BaseRsp fetchBaseInfo(@RequestBody GetBaseInfoRequest requestData) {
        BaseRsp rsp = new BaseRsp();
        rsp.setId(requestData.getId());
        rsp.setSuccess(true);
        if (requestData.trigger) {
            rsp.setData(LocomotionModule.getInstance().fetchBaseInfo());
        }
        return rsp;
    }


}
