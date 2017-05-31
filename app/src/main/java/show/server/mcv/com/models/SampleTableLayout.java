package show.server.mcv.com.models;

/**
 * Created by Matthew on 9/1/2016.
 */
public class SampleTableLayout {

    public static final SampleTableLayout[] TABLE_DATA =
            new SampleTableLayout[]{new SampleTableLayout("Bob Burl", "$ 300.00"),
                    new SampleTableLayout("Rick Jones", "$ 600.00"),
                    new SampleTableLayout("Joe Nice", "$ 900.00"),
                    new SampleTableLayout("Candy Brown", "$ 6600.00"),
                    new SampleTableLayout("Denver Sanchez", "$ 200.00"),
                    new SampleTableLayout("Ruby Clark", "$ 700.00"),
                    new SampleTableLayout("Ken Smith", "$ 900.00"),
                    new SampleTableLayout("Rose Johns", "$ 300.00"),
                    new SampleTableLayout("Sally Golden", "$ 200.00"),
                    new SampleTableLayout("Greg Derick", "$ 700.00"),
                    new SampleTableLayout("Alan Scott", "$ 500.00"),
                    new SampleTableLayout("Dan Green", "$ 300.00")};

    public String column1;
    public String column2;

    public SampleTableLayout(String column1Data, String column2Data) {
        column1 = column1Data;
        column2 = column2Data;
    }
}
