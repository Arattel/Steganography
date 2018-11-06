import lombok.*;

@Getter
@Setter
public class BytePixel {
    private String a;
    private String r;
    private String g;
    private String b;
    public BytePixel(String rgb){
        this.a = rgb.substring(0, 8);
        this.r = rgb.substring(8, 16);
        this.g = rgb.substring(16, 24);
        this.b = rgb.substring(24);
    }
}
