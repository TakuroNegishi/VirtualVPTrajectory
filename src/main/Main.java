package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainFrame.fxml"));
        String dir = System.getProperty("user.dir");
        String[] dirArray = dir.split("\\\\");
        primaryStage.setTitle(dirArray[dirArray.length - 1]); // プロジェクト名
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
//        for (int i = 1; i <= 8; i++) {
//            testStep(i);
//        }
//        System.out.println("");
    }

    public static void main(String[] args) { launch(args); }

    void testStep(int step) {
        double eyeX = -1.015;
        double eyeY = 4.0;
        double eyeEndX = -1.015;
        double eyeEndY = 0;
        double vecAX = eyeEndX - eyeX;
        double vecAY = eyeEndY - eyeY;
        double vecBX = 0 - eyeX;
        final double stride = 0.5;
        double vecBY = stride * step - eyeY;
//        System.out.println("vecAX = " + vecAX);
//        System.out.println("vecAY = " + vecAY);
//        System.out.println("vecBX = " + vecBX);
//        System.out.println("vecBY = " + vecBY);

//      cosθ = ( AとBの内積 ) / (Aの長さ * Bの長さ)
        double cos = (vecAX * vecBX + vecAY * vecBY) / (length(vecAX, vecAY) * length(vecBX, vecBY));
        double acos = Math.toDegrees(Math.acos(cos));
        System.out.println("0," + (step * stride) + "," + (90 + acos));
    }

    double length(double vx, double vy) {
        return Math.sqrt(vx * vx + vy * vy);
    }

    void testDir(double angle) {
        double eyeX = -1.015;
        double eyeY = 4.0;
        double eyeEndX = -1.015;
        double eyeEndY = -7.0;
        double diffX = eyeEndX - eyeX;
        double diffY = eyeEndY - eyeY;
        double r = Math.toRadians(angle); // 歩行者とは逆向きに回転する
        // 現在位置(x,y)を原点(0,0)として回転
        eyeEndX = diffX * Math.cos(r) - diffY * Math.sin(r) + eyeX;
        eyeEndY = diffX * Math.sin(r) + diffY * Math.cos(r) + eyeY;
//        System.out.println(eyeEndX + ", " + eyeEndY);
        double dirX = 0;
        double dirY = 0;
        double dirEndX = 0;
        double dirEndY = 10;

//        面積S1　= {(P4.X - P2.X) * (P1.Y - P2.Y) - (P4.Y - P2.Y) * (P1.X - P2.X)} / 2
//        面積S2　= {(P4.X - P2.X) * (P2.Y - P3.Y) - (P4.Y - P2.Y) * (P2.X - P3.X)} / 2
        double s1 = ((dirEndX - dirX) * (eyeY - dirY) - (dirEndY - dirY) * (eyeX - dirX)) / 2;
        double s2 = ((dirEndX - dirX) * (dirY - eyeEndY) - (dirEndY - dirY) * (dirX - eyeEndX)) / 2;
//        C1.X　= P1.X + (P3.X - P1.X) * S1 / (S1 + S2)
//        C1.Y　= P1.Y + (P3.Y - P1.Y) * S1 / (S1 + S2)
        double cx = eyeX + (eyeEndX - eyeX) * s1 / (s1 + s2);
        double cy = eyeY + (eyeEndY - eyeY) * s1 / (s1 + s2);
        System.out.println(cx + "," + cy + "," + (90 + angle));
    }
}
