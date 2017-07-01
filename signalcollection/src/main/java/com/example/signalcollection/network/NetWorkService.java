package com.example.signalcollection.network;

import com.example.signalcollection.bean.AddBusinessBody;
import com.example.signalcollection.bean.AddBusinessBodyV2;
import com.example.signalcollection.bean.AddBusinessResult;
import com.example.signalcollection.bean.AddSearchTypeExpropResult;
import com.example.signalcollection.bean.AreaType;
import com.example.signalcollection.bean.BusinessTypeList;
import com.example.signalcollection.bean.CPResult;
import com.example.signalcollection.bean.CityList;
import com.example.signalcollection.bean.CompanyResult;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.Exprop;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.bean.Predefine;
import com.example.signalcollection.bean.PredefineRequest;
import com.example.signalcollection.bean.RegionList;
import com.example.signalcollection.bean.RegisterResult;
import com.example.signalcollection.bean.ReportWrongBody;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.bean.SimilarResult;
import com.example.signalcollection.bean.StatusResult;
import com.example.signalcollection.bean.UnAssignedRequest;
import com.example.signalcollection.bean.UserBehavior;
import com.example.signalcollection.bean.UserInfoRequest;
import com.example.signalcollection.bean.WorkListResult;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by hehe on 2016/4/20.
 */
public interface NetWorkService {

    public interface Work {


        //获取进行中的任务
        @POST("api/nmaps/task/get.j5")
        Observable<WorkListResult> get(@Body UserInfoRequest userInfoRequest);


        //获取待审核的任务
        @POST("api/nmaps/task/getChecking.j5")
        Observable<WorkListResult> getChecking(@Body UserInfoRequest userInfoRequest);

        //获取已审核的任务
        @POST("api/nmaps/task/getChecked.j5")
        Observable<WorkListResult> getChecked(@Body UserInfoRequest userInfoRequest);


        //根据商圈（目的地）类型获取采集点类型
        @POST("api/nmaps/cp/get.j5")
        Observable<CPResult> getCp(@Body AreaType dataBean);


        //获取扩展属性值
        @POST("api/nmaps/exprop/get.j5")
        Observable<Exprop> getExprop(@Body CPResult.DataBean dataBean);

        //获取扩展属性值
        @POST("api/nmaps/exprop/get.j5")
        Observable<Exprop> getExpropByCpCode(@Body Map<String, String> dataBean);

        /**
         * （采集）获取扩展属性的预设值
         *
         * @deprecated
         **/

        //（采集）获取扩展属性的预设值 2017-3-10更新v2
        //@POST("api/nmaps/exprop/predefine/get.j5")
        @POST("api/nmaps/exprop/v2/predefine/get.j5")
        Observable<Predefine> getPredefine(@Body PredefineRequest predefineRequest);


        //数据提交
        @POST("api/nmaps/task/upload.j5")
        Observable<DefaultResult> upload(@Body NmpReportData nmpReportData);


        //获取城市列表
        @POST("api/nmaps/city/get.j5")
        Observable<CityList> getCityList();

        //获取商圈类型列表
        @POST("api/nmaps/areaType/get.j5")
        Observable<BusinessTypeList> getBusinessTypeList();

        //用户添加商圈
        @POST("api/nmaps/task/userAddArea.j5")
        Observable<AddBusinessResult> addBusiness(@Body AddBusinessBody addBusinessBody);


        //用户添加商圈V2
        @POST("api/nmaps/task/v2/userAddArea.j5")
        Observable<AddBusinessResult> userAddAreaTask(@Body AddBusinessBodyV2 addBusinessBody);


        //上报商圈错误
        @POST("api/nmaps/task/reportWrongArea.j5")
        Observable<DefaultResult> reportWrong(@Body ReportWrongBody reportWrongBody);


        //用户行为上报
        @POST("api/userBehavior/trigger.j5")
        Observable<DefaultResult> reportUserBehavior(@Body UserBehavior userBehavior);

        //查找商圈
        @POST("api/nmaps/area/search.j5")
        Observable<WorkListResult> searchAreaTask(@Body Map<String, String> keyword);

        //查找类似商圈
        @POST("api/nmaps/area/searchBySeg.j5")
        Observable<SimilarResult> searchSimilarAreaTask(@Body Map<String, String> keyword);


        //数据提交第二版
        @POST("api/nmaps/task/v2/uploadData.j5")
        Observable<DefaultResult> uploadData(@Body NmpReportData nmpReportData);


        //任务提交第二版
        @POST("api/nmaps/task/v2/submit.j5")
        Observable<DefaultResult> submit(@Body Map<String, String> map);


        // 获取注册手机验证码
        @POST("api/nmaps/user/getRegisterCode.j5")
        Observable<RegisterResult> getRegisterCode(@Body Map<String, String> map);


        //获取公司列表
        @POST("api/nmaps//company/get.j5")
        Observable<CompanyResult> getCompany();


        //注册
        @POST("api/nmaps/user/doRegister.j5")
        Observable<DefaultResult> register(@Body Map<String, String> map);

        //获取登录验证码
        @POST("api/nmaps/user/getLoginCode.j5")
        Observable<RegisterResult> getLoginCode(@Body Map<String, String> map);


        //登录
        @POST("api/nmaps/user/doLogin.j5")
        Observable<DefaultResult> login(@Body Map<String, String> map);


        //登录第二版
        @POST("api/nmaps/user/doLogin/v2.j5")
        Observable<DefaultResult> loginV2(@Body Map<String, Object> map);


        //领取列表
        @POST("api/nmaps/task/getUnAssigned.j5")
        Observable<WorkListResult> getUnAssigned(@Body UnAssignedRequest request);

        //模糊查询未领取的任务
        @POST("api/nmaps/task/getByCondition.j5")
        Observable<WorkListResult> getByCondition(@Body UnAssignedRequest request);

        //领取任务
        @POST("api/nmaps/task/userDraw.j5")
        Observable<DefaultResult> userDraw(@Body Map<String, String> map);

        //通过cityCode获取区域列表
        @POST("api/nmaps/cityRegion/get.j5")
        Observable<RegionList> getRegionList(@Body Map<String, String> map);

        //修改个人信息
        @POST("api/nmaps/user/updateUserInfo.j5")
        Observable<DefaultResult> modifiyPersonMsg(@Body Map<String, Object> map);

        //获取个人信息
        @POST("api/nmaps/user/getUserInfo.j5")
        Observable<PersonInfoResult> getPersonMsg(@Body Map<String, String> map);


        //动态url获取搜索数据
        @POST
        Observable<SearchResult> getSearch(@Url String url, @Body Map<String, String> map);


        //获取添加搜索类型的扩展属性
        @POST("api/nmaps/exprop/brand/get.j5")
        Observable<AddSearchTypeExpropResult> getSearchTypeExprop(@Body Map<String, String> map);


        @POST("area/updateLatiAndLongi.j5")
        Observable<DefaultResult> updateLatAndLong(@Body Map<String, Object> map);


        //根据商圈类型获取商圈扩展属性{"areaTypeCode": "AT-HOTEL",}
        @POST("api/nmaps/area/getexprop.j5")
        Observable<Exprop> getExpropByAreaTypeCode(@Body Map<String, String> map);

        //根据商圈类型获取商圈扩展属性{"refAreaTypeCode": "EXP_STARS"," refExPropCode": "AT_HOTEL"}
        @POST("api/nmaps/area/prop/getpredefine.j5")
        Observable<Predefine> getPredefineByAreaCodeAndExpropCode(@Body Map<String, String> map);


        //获取审核后的任务
        @POST("api/nmaps/task/getOnlineChecking.j5")
        Observable<WorkListResult> getOnlineChecking(@Body UserInfoRequest username);


        //获取任务的提交状态
        @POST("api/nmaps/task/getDataSubmitStatus.j5")
        Observable<StatusResult> getDataSubmitStatus(@Body Map<String, List<String>> map);

    }

}
