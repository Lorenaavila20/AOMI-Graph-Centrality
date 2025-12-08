public class AmostraPonto {
    private double latitude;
    private double longitude;
    private double densidadeParticulas; 

    public AmostraPonto(double latitude, double longitude, double densidadeParticulas) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.densidadeParticulas = densidadeParticulas;
    }

    public double getLat() {
        return latitude;
    }

    public double getLon() {
        return longitude;
    }

    public double getDensidade() {
        return densidadeParticulas;
    }

    @Override
    public String toString() {
        return String.format("AmostraPonto [Lat=%.2f, Lon=%.2f, Dens=%.2f]", latitude, longitude, densidadeParticulas);
    }
}