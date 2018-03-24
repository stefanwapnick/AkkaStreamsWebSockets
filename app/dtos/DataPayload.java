package dtos;

import java.util.List;

public class DataPayload {

    public int id;
    public List<Integer> numbers;

    public DataPayload(int id, List<Integer> numbers) {
        this.id = id;
        this.numbers = numbers;
    }
}
