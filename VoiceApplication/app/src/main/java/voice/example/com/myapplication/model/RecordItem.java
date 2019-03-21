package voice.example.com.myapplication.model;

public class RecordItem {
    private String mFileName;
    private String mQueryText;

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String name) {
        this.mFileName = name;
    }

    public String getQueryText() {
        return mQueryText;
    }

    public void setQueryText(String text) {
        this.mQueryText = text;
    }
    @Override
    public int hashCode() {
        return this.mFileName.hashCode()+mQueryText.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if(obj instanceof RecordItem) {
            RecordItem i = (RecordItem)obj;
            if(this.getQueryText().equals(i.getQueryText())&&this.getFileName().equals(i.getFileName()))
            {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
