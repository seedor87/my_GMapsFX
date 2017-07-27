import com.lynden.gmapsfx.javascript.object.LatLong;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class KMLParser {

    public static ArrayList<ArrayList<double[]>> getCoordinateArrayLists(String filename) {
        ArrayList<ArrayList<double[]>> allTracks = new ArrayList<ArrayList<double[]>>();

        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = new FileInputStream(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String str;
            String buffer;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
            String html = buf.toString();
            Document doc = Jsoup.parse(html, "", Parser.xmlParser());
            ArrayList<String> tracksString = new ArrayList<String>();
            for (Element e : doc.select("coordinates")) {
                tracksString.add(e.toString().replace("<coordinates>", "").replace("</coordinates>", ""));
            }

            for (int i = 0; i < tracksString.size(); i++) {
                ArrayList<double[]> oneTrack = new ArrayList<double[]>();
                ArrayList<String> oneTrackString = new ArrayList<String>(Arrays.asList(tracksString.get(i).split("\\s+")));
                for (int k = 1; k < oneTrackString.size(); k++) {
                    double[] latLng = new double[2];
                    latLng[0] = Double.parseDouble(oneTrackString.get(k).split(",")[0]);
                    latLng[1] = Double.parseDouble(oneTrackString.get(k).split(",")[1]);
                    oneTrack.add(latLng);
                }
                allTracks.add(oneTrack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allTracks;
    }

    public static ArrayList<LatLong[]> getCoordinateArrays(String filename) {
        ArrayList<LatLong[]> allTracks = new ArrayList<LatLong[]>();

        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = new FileInputStream(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String str;
            String buffer;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
            String html = buf.toString();
            Document doc = Jsoup.parse(html, "", Parser.xmlParser());
            ArrayList<String> tracksString = new ArrayList<String>();
            for (Element e : doc.select("coordinates")) {
                tracksString.add(e.toString().replace("<coordinates>", "").replace("</coordinates>", ""));
            }

            for (int i = 0; i < tracksString.size(); i++) {
                LatLong[] oneTrack = new LatLong[]{};
                ArrayList<String> oneTrackString = new ArrayList<String>(Arrays.asList(tracksString.get(i).split("\\s+")));
                for (int k = 1; k < oneTrackString.size(); k++) {
                    LatLong latLng = new LatLong(
                            Double.parseDouble(oneTrackString.get(k).split(",")[0]),
                            Double.parseDouble(oneTrackString.get(k).split(",")[1]));
                    oneTrack[i] = latLng;
                }
                allTracks.add(oneTrack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allTracks;
    }
}