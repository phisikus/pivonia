import java.util.Objects;

class TestSerializable {
    private String name;
    private int number;

    public TestSerializable() {
    }

    public TestSerializable(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSerializable that = (TestSerializable) o;
        return number == that.number &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number);
    }
}
