import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Steganography {
    private static BytePixel[][] imageIntoByteArray(String image) throws IOException {
        File f = new File(image);
        BufferedImage img = ImageIO.read(f);
        int height = img.getHeight();
        int width = img.getWidth();
        BytePixel[][] imageArray = new BytePixel[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int p = img.getRGB(j, i);
                imageArray[i][j] = new BytePixel(Integer.toBinaryString(p));
            }
        }
        return imageArray;
    }

    private static String messageIntoByte(String message) {
        int length = message.length();
        String mesageString = "";
        String size = Integer.toBinaryString(length);
        for (int j = 0; j < 8 - size.length(); j++) {
            mesageString += "0";
        }
        mesageString += size;
        int length_byte = mesageString.length();
        for (int i = 0; i < length; i++) {
            String character = Integer.toBinaryString((int) message.charAt(i));
            int lengthBefore = mesageString.length();
            for (int j = 0; j < 8 - character.length(); j++) {
                mesageString += "0";
            }
            mesageString += character;
        }
        return mesageString;
    }

    private static String messageFromByteString(String bytes) {
        int messageSize = Integer.parseInt(bytes.substring(0, 8), 2);
        bytes = bytes.substring(8);
        String decoded = "";
        for (int i = 0; i < messageSize; i++) {
            String charByte = bytes.substring(i * 8, (i + 1) * 8);
            char asciiCharacter = (char) Integer.parseInt(charByte, 2);
            decoded += asciiCharacter;
        }
        return decoded;
    }

    public static void main(String[] args) throws IOException {
        Steganography st = new Steganography();
        st.encodeImageFile("test.jpg", "Helo world");
        System.out.println(st.readCodeFromImage("encoded_image.png"));
    }

    private BytePixel[][] encodeImage(BytePixel[][] imageWithoutCode, String message) {
        String stringToAdd = messageIntoByte(message);
        BytePixel[][] result = imageWithoutCode.clone();
        int imageHeight = imageWithoutCode.length;
        int imageWidth = imageWithoutCode[0].length;
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                String BytesToAdd;
                if (stringToAdd.length() == 8) {
                    BytesToAdd = stringToAdd;
                    stringToAdd = "";
                } else if (stringToAdd.length() == 0) {
                    break;
                } else {
                    BytesToAdd = stringToAdd.substring(0, 8);
                    stringToAdd = stringToAdd.substring(8);
                }
                String a = result[i][j].getA();
                a = a.substring(0, 6) + BytesToAdd.substring(0, 2);
                result[i][j].setA(a);
                String r = result[i][j].getR();
                r = r.substring(0, 6) + BytesToAdd.substring(2, 4);
                result[i][j].setR(r);
                String g = result[i][j].getG();
                g = g.substring(0, 6) + BytesToAdd.substring(4, 6);
                result[i][j].setG(g);
                String b = result[i][j].getB();
                b = b.substring(0, 6) + BytesToAdd.substring(6);
                result[i][j].setB(b);
            }
        }
        return result;
    }

    private void byteArrayIntoImage(BytePixel[][] imageWithCode) throws IOException {
        String path = "encoded_image.png";
        int height = imageWithCode.length;
        int width = imageWithCode[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int p = image.getRGB(j, i);
                int a = Integer.parseInt(imageWithCode[i][j].getA(), 2);
                int r = Integer.parseInt(imageWithCode[i][j].getR(), 2);
                int g = Integer.parseInt(imageWithCode[i][j].getG(), 2);
                int b = Integer.parseInt(imageWithCode[i][j].getB(), 2);
                p = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(j, i, p);
            }
        }
        File ImageFile = new File(path);
        if(ImageFile.exists()){
            ImageFile.delete();
        }
        ImageIO.write(image, "png", ImageFile);
    }

    private String readBinaryFromImage(BytePixel[][] image) {
        int length;
        String lengthString = "";
        BytePixel lengthPixel = image[0][0];
        lengthString += lengthPixel.getA().substring(6);
        lengthString += lengthPixel.getR().substring(6);
        lengthString += lengthPixel.getG().substring(6);
        lengthString += lengthPixel.getB().substring(6);
        length = Integer.parseInt(lengthString, 2);
        String message = "";
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                if (length == 0) {
                    break;
                } else if (i != 0 || j != 0) {
                    BytePixel charPixel = image[i][j];
                    message += charPixel.getA().substring(6);
                    message += charPixel.getR().substring(6);
                    message += charPixel.getG().substring(6);
                    message += charPixel.getB().substring(6);
                    length--;
                }
            }
        }
        message = messageFromByteString(lengthString + message);
        return message;
    }

    public String readCodeFromImage(String image) throws IOException {
        BytePixel[][] byte_arr = imageIntoByteArray(image);
        return readBinaryFromImage(byte_arr);
    }

    public void encodeImageFile(String image, String message) throws IOException {
        BytePixel[][] byte_arr = imageIntoByteArray(image);
        byte_arr = encodeImage(byte_arr, message);
        byteArrayIntoImage(byte_arr);
    }
}
