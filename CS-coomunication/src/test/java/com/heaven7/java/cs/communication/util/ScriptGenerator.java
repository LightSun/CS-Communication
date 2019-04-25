package com.heaven7.java.cs.communication.util;

import com.heaven7.java.base.util.FileUtils;
import com.heaven7.java.base.util.Platforms;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.security.KeyPair;
import java.util.Base64;
import java.util.List;

/**
 * @author heaven7
 */
public class ScriptGenerator {

    @Test
    public void test1() throws Exception{
        KeyPair keyPair = RSACoder.initKeys();
        String publicStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        System.out.println("publicStr: " + publicStr);
        System.out.println("privateStr: " + privateStr);
    }

    @Test
    public void generateCSBat()  throws Exception{
        KeyPair keyPair = RSACoder.initKeys();
        String publicStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        genImpl(publicStr, privateStr);
    }

    private void genImpl(String publicStr, String privateStr) {
        //java -cp    or   java -jar
        String format = "java -Dfile.encoding=UTF-8 -cp %s %s isClient=%s rsaKey=%s host=%s port=%d";
        final String jarName = "all.jar";
        //FileUtils.getFiles()
        String url = ScriptGenerator.class.getResource("").toString();
        if(url.startsWith("file:/")){
            url = url.substring(6);
        }
        String libDir = url.substring(0, url.indexOf("classes")) + "libs";
        System.out.println(libDir);

        List<String> extraJars = FileUtils.getFiles(new File(libDir), "jar", new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getAbsolutePath();
                String name = FileUtils.getFileName(path);
                if (name.startsWith("cs-coomunication")) {
                    return false;
                }
                /*if (name.equals(jarName.substring(0, jarName.indexOf(".")))) {
                    return false;
                }*/
                return true;
            }
        });
        StringBuilder sb = new StringBuilder();
        for (int i = 0 , size = extraJars.size() ; i < size ; i ++){
            sb.append(extraJars.get(i));
            if(i != size - 1){
                sb.append(";");
            }
        }
        String classPath = sb.toString();

        String host = "127.0.0.1";
        int port = 62381;
        String mainClass = "com.heaven7.java.cs.communication.sample.Launcher";
        //generate server.bat
        String content = String.format(format, classPath, mainClass, false, privateStr, host, port)
                + Platforms.getNewLine() + "@PAUSE";

        FileUtils.writeTo(libDir + File.separator + "server.bat", content);
        //client.bat
        content = String.format(format, classPath, mainClass, true, publicStr, host, port)
                + Platforms.getNewLine() + "@PAUSE";
        FileUtils.writeTo(libDir + File.separator + "client.bat", content);
    }
}
