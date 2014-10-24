import java.util.List;

public class AxisData<T extends Number> {
    private String xAxisName;
    private String yAxisName;

    private String seriesName;

    private List<T> xDataList;
    private List<T> yDataList;

    public AxisData(String xAxisName, String yAxisName, String seriesName, List<T> xDataList, List<T> yDataList) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        this.seriesName = seriesName;
        this.xDataList = xDataList;
        this.yDataList = yDataList;
    }

    public String getxAxisName() {
        return xAxisName;
    }

    public void setxAxisName(String xAxisName) {
        this.xAxisName = xAxisName;
    }

    public String getyAxisName() {
        return yAxisName;
    }

    public void setyAxisName(String yAxisName) {
        this.yAxisName = yAxisName;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public List<T> getxDataList() {
        return xDataList;
    }

    public void setxDataList(List<T> xDataList) {
        this.xDataList = xDataList;
    }

    public List<T> getyDataList() {
        return yDataList;
    }

    public void setyDataList(List<T> yDataList) {
        this.yDataList = yDataList;
    }

    public int getSize(){
        return Math.min(xDataList.size(), yDataList.size());
    }
}
