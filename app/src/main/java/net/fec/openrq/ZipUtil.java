package net.fec.openrq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
     * 閸樺缂夋稉锟芥稉顏呮瀮娴犺泛銇�
     *
     * @throws IOException
     */
    public Path zipDirectory(String path) throws IOException {
        File file = new File(path);
        String parent = file.getParent();
        File zipFile = new File(parent, file.getName() + ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zip(zos, file, file.getName());
        zos.flush();
        zos.close();
        return zipFile.toPath();
    }

    /**
     *
     * @param zos
     *            閸樺缂夋潏鎾冲毉濞达拷
     * @param file
     *            瑜版挸澧犻棁锟界憰浣稿竾缂傗晝娈戦弬鍥︽
     * @param path
     *            瑜版挸澧犻弬鍥︽閻╃顕禍搴″竾缂傗晜鏋冩禒璺恒仚閻ㄥ嫯鐭惧锟�
     * @throws IOException
     */
    private void zip(ZipOutputStream zos, File file, String path) throws IOException {
        // 妫ｆ牕鍘涢崚銈嗘焽閺勵垱鏋冩禒璁圭礉鏉╂ɑ妲搁弬鍥︽婢剁櫢绱濋弬鍥︽閻╁瓨甯撮崘娆忓弳閻╊喖缍嶆潻娑樺弳閻愮櫢绱濋弬鍥︽婢剁懓鍨柆宥呭坊
        if (file.isDirectory()) {
            ZipEntry entry = new ZipEntry(path + File.separator);// 閺傚洣娆㈡径鍦畱閻╊喖缍嶆潻娑樺弳閻愮懓绻�妞よ浜掗崥宥囆為崚鍡涙缁楋妇绮ㄧ亸锟�
            zos.putNextEntry(entry);
            File[] files = file.listFiles();
            for (File x : files) {
                zip(zos, x, path + File.separator + x.getName());
            }
        } else {
            FileInputStream fis = new FileInputStream(file);// 閻╊喖缍嶆潻娑樺弳閻愬湱娈戦崥宥呯摟閺勵垱鏋冩禒璺烘躬閸樺缂夐弬鍥︽娑擃厾娈戠捄顖氱窞
            ZipEntry entry = new ZipEntry(path);
            zos.putNextEntry(entry);// 瀵よ櫣鐝涙稉锟芥稉顏嗘窗瑜版洝绻橀崗銉у仯

            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.flush();
            fis.close();
            zos.closeEntry();// 閸忔娊妫磋ぐ鎾冲閻╊喖缍嶆潻娑樺弳閻愮櫢绱濈亸鍡氱翻閸忋儲绁︾粔璇插З娑撳绔存稉顏嗘窗瑜版洝绻橀崗銉у仯
        }
    }

}
