package g525databinding

class Offer {

    String name
    List<Translation> translations

    static hasMany = [translations: Translation]


    @Override
    public String toString() {
        return "Offer{" +
                "name='" + name + '\'' +
                ", translations=" + translations +
                '}';
    }
}
