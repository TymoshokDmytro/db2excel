package auchan.model;

public class Data {
    private Object[] DataArray;
    private int size;

    public Data(Object... DataArray) {
        this.DataArray = new Object[DataArray.length];
        for (int i = 0; i < DataArray.length; i++) {
            this.DataArray[i] = DataArray[i];
            if (DataArray[i] == null) {
                this.DataArray[i] = "null";
            }
        }
        size = DataArray.length;
    }

    public Data(int size) {
        this.DataArray = new Object[size];
        for (int i = 0; i < size; i++) {
            this.DataArray[i] = "null";
        }
        this.size = size;
    }

    public Object get(int index) {
        return DataArray[index];
    }

    public void set(int index, Object obj) {
        if (obj == null) {
            this.DataArray[index] = "null";
        } else {
            this.DataArray[index] = obj;
        }
    }

    public int size() {
        return size;
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < size; i++) {
            temp.append(this.DataArray[i].toString()).append(" | ");
        }
        return temp.substring(0, temp.length() - 3);
    }
}
