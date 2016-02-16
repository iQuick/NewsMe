package me.imli.newme;

/**
 * Created by Em on 2015/12/28.
 */
public class Test {
//    private String initContent(String content, boolean night, boolean flag) {
//        try {
//            InputStream inputStream = getResources().getAssets().open("discover.html");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    inputStream), 16 * 1024);
//            StringBuilder sBuilder = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sBuilder.append(line + "\n");
//            }
//            String modelHtml = sBuilder.toString();
//            inputStream.close();
//            reader.close();
//
//            String contentNew = modelHtml.replace(
//                    "<--@#$%discoverContent@#$%-->", content);
//            if (night) {
//                contentNew = contentNew.replace("<--@#$%colorfontsize2@#$%-->",
//                        "color:#8f8f8f ;");
//            } else {
//                contentNew = contentNew.replace("<--@#$%colorfontsize2@#$%-->",
//                        "color:#333333 ;");
//            }
//            if (flag) {
//                contentNew = contentNew.replace(
//                        "<--@#$%colorbackground@#$%-->", "background:#B4CDE6");
//            } else {
//                contentNew = contentNew.replace(
//                        "<--@#$%colorbackground@#$%-->", "background:#F9BADA");
//            }
//            return contentNew;
//
//        } catch (IOException e) {
//// TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }
}
