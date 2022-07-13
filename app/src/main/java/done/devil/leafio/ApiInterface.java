package done.devil.leafio;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {
    @Multipart
    @POST("api/")
    Call<ResultModel> callUploadApi(@Part MultipartBody.Part image);

    @Multipart
    @POST("upload_file/RestApi/multi_upload.php")
    Call<ResultModel> callMultipleUploadApi(@Part List<MultipartBody.Part> image);
}
