package util;


import javafx.scene.text.Font;

/**
 * 字体：Font.PLAIN(プレーン体)<br>
 * サイズ：<br>
 * <table border=0>
 * <tr><td>X_SMALL</td>	<td>=</td>	<td>13</td></tr>
 * <tr><td>SMALL</td>	<td>=</td>	<td>15</td></tr>
 * <tr><td>NORMAL</td>	<td>=</td>	<td>18</td></tr>
 * <tr><td>LARGE</td>	<td>=</td>	<td>22</td></tr>
 * <tr><td>X_LARGE</td>	<td>=</td>	<td>25</td></tr>
 * </table>
 *
 * @author 15k0013 Negishi Takuro
 *
 */
public class FontList {
    /** インスタンス化は禁止 */
    private FontList(){};
    public static final Font X_SMALL_FONT = new Font(10);
    public static final Font SMALL_FONT = new Font(15);
    public static final Font NORMAL_FONT = new Font(20);
    public static final Font LARGE_FONT = new Font(23);
    public static final Font X_LARGE_FONT = new Font(26);

}
