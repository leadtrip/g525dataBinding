package g525databinding

class Translation {

    String name
    Locale locale

    static belongsTo = [offer: Offer]

    static constraints = {
        offer unique: ['locale']
    }


    @Override
    public String toString() {
        return "Translation{" +
                "name='" + name + '\'' +
                ", locale=" + locale +
                '}';
    }
}
