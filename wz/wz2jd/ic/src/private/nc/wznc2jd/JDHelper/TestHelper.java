package nc.wznc2jd.JDHelper;
import com.jd.open.api.sdk.*;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.request.ECLP.*;

public class TestHelper {

	String SERVER_URL="https://api.jd.com/routerjson";
	String accessToken=ConfigHelper.getInstance().getAccessToken();
	String appKey=ConfigHelper.getInstance().getAppKey();
	String appSecret=ConfigHelper.getInstance().getAppSecret();
	public EclpMasterQueryShopResponse Create() throws Exception{
	   JdClient client=new DefaultJdClient(SERVER_URL,accessToken,appKey,appSecret);

		EclpMasterQueryShopRequest request=new EclpMasterQueryShopRequest();
		request.setDeptNo( "EBU4418046513690" );

		EclpMasterQueryShopResponse response=client.execute(request);

		return response;
	}

}
