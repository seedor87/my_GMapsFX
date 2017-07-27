
import com.lynden.gmapsfx.javascript.object.LatLong;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by eliakah on 9/16/16.
 * This class is a more efficient a revised version of the kmlGenerator,
 * it can be used outside this project very easily
 */
public class KMLBuilder {

    String fileName = ""; //file name
    String description = ""; //file description

    /**
     * empty constructor
     */
    public KMLBuilder() {

    }

    /**
     * constructor with fileName specified
     *
     * @param fileName
     * @param description
     */
    public KMLBuilder(String fileName, String description) {
        this.fileName = fileName + ".kml";
        this.description = description;
    }

    /**
     * This generates & writes the KML file using the helper methods.
     *
     * @param content
     * @throws IOException
     */
    public void createFile(String content) throws IOException {
        if (fileName == "") {
            fileName = getFileName();
        }

        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "<Document>\n" +
                "  <name>" + fileName + "</name>\n" +
                "  <description>" + description + "</description>\n" +
                "  <open>1</open>\n" +
                content + "\n" +
                "</Document>\n" +
                "</kml>";


        //create file
        File outPutFile = new File(fileName);//gets file name from timestamp

        if (outPutFile.createNewFile()) {
            String text = "";
            PrintWriter writer = new PrintWriter(
                    fileName, "UTF-8");
            writer.write(str);
            writer.close();
        } else {
            System.out.println("File Creation Unsuccessful!.");
        }


    }


    /**
     * Creates placemark string.
     *
     * @param latlon the point
     * @param des   the des
     * @return the string
     */
    public String placemark(LatLong latlon, String title, String des) {
        String str = "<Placemark>\n" +
                "  <name>" + title + "</name>\n" +
                "  <description>" + des + "</description>\n" +
                "  <Point>\n" +
                "    <coordinates>" + latlon.getLongitude() + "," + latlon.getLatitude() + ",0</coordinates>\n" +
                "  </Point>\n" +
                "</Placemark>";

        return str;
    }

    /**
     * @param outerBound
     * @param inBound
     * @return string
     */
    public String polygon(List<LatLong> outerBound, List<LatLong> inBound, String title) {
        String str = "<Placemark>\n" +
                "    <name>" + title + "</name>\n" +
                "    <Polygon>\n" +
                "      <extrude>1</extrude>\n" +
                "      <altitudeMode>clampToGround</altitudeMode>\n" +
                "      <outerBoundaryIs>\n" +
                "        <LinearRing>\n" +
                "          <coordinates>\n";

        //inserting outer bounds of polygon
        for (int i = 0; i < outerBound.size(); i++) {
            str += "\t" + outerBound.get(i).getLongitude() + "," + outerBound.get(i).getLatitude() + ",0\n";
        }

        str += "          </coordinates>\n" +
                "        </LinearRing>\n" +
                "      </outerBoundaryIs>\n";

        //if inner bound list is not empty or = null
        if (!(inBound == null || inBound.size() == 0)) {
            str += "      <innerBoundaryIs>\n" +
                    "        <LinearRing>\n" +
                    "          <coordinates>\n";
            for (int i = 0; i < outerBound.size(); i++) {
                str += outerBound.get(i).getLongitude() + "," + outerBound.get(i).getLatitude() + ",0\n";
            }
            str += "          </coordinates>\n" +
                    "        </LinearRing>\n" +
                    "      </innerBoundaryIs>\n";
        }

        str += "    </Polygon>\n" +
                "  </Placemark>";

        return str;
    }


    /**
     * Create path string.
     *
     * @return the string
     */
    public String path(List<LatLong> path, String title) {
        String str = "<Placemark>\n" +
                "<name>" + title + "</name>\n" +
                "<LineString>\n" +
                "<extrude>1</extrude>\n" +
                "<gx:altitudeMode>clampToGround</gx:altitudeMode>\n" +
                "<coordinates>";

        //inserting points that make up the path
        for (int i = 0; i < path.size(); i++) {
            str += path.get(i).getLongitude() + "," + path.get(i).getLatitude() + ",400\n";
        }
        str += "</coordinates>\n" +
                "</LineString>\n" +
                "</Placemark>\n";

        return str;
    }


    /**
     * Generates a file name based on date and time
     *
     * @return File name as a string.
     */
    public String getFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        timeStamp += ".kml";
        return timeStamp;
    }


}