public class HttpClientTest {
    public static void main(String[] args) throws Exception {
        HttpClientResult httpClientResult = HttpClientUtil.doGet("http://localhost:8080");
        System.out.println(httpClientResult.getContent());
    }
}
