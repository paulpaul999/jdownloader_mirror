package org.jdownloader.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jd.config.SubConfiguration;
import jd.controlling.linkcollector.LinknameCleaner;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.PackageInfo;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.nutils.encoding.Base64;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.HTMLParser;
import jd.plugins.ContainerStatus;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginsC;
import jd.utils.JDHexUtils;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.Files;
import org.appwork.utils.Hash;
import org.appwork.utils.StringUtils;
import org.appwork.utils.parser.UrlQuery;
import org.jdownloader.controlling.filter.CompiledFiletypeFilter;
import org.jdownloader.controlling.filter.CompiledFiletypeFilter.ExtensionsFilterInterface;
import org.jdownloader.logging.LogController;
import org.jdownloader.updatev2.UpdateController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class D extends PluginsC {
    private byte[]                  b3;
    private byte[]                  d;
    private HashMap<String, String> header;

    public D() {
        super("DLC", "file:/.+\\.dlc$", "$Revision: 45974 $");
        b3 = new byte[] { 77, 69, 84, 65, 45, 73, 78, 70, 47, 74, 68, 79, 87, 78, 76, 79, 65, 46, 68, 83, 65 };
        d = new byte[] { -44, 47, 74, 116, 56, -46, 20, 9, 17, -53, 0, 8, -47, 121, 1, 75 };
        // kk = (byte[]) SubConfiguration.getConfig(new String(new byte[] { 97,
        // 112, 99, 107, 97, 103, 101, 1 })).getProperty(new String(new byte[] {
        // 97, 112, 99, 107, 97, 103, 101, 1 }), new byte[] { 112, 97, 99, 107,
        // 97, 103, 101 });
        //
    }

    public D newPluginInstance() {
        return new D();
    }

    public static String filterString(final String str) {
        final String allowed = "QWERTZUIOPÜASDFGHJKLÖÄYXCVBNMqwertzuiopasdfghjklyxcvbnmöäü;:,._-&$%(){}#~+ 1234567890<>='\"/";
        return Encoding.filterString(str, allowed);
    }

    // //@Override
    public ContainerStatus callDecryption(File d) {
        ContainerStatus cs = new ContainerStatus(d);
        cs.setStatus(ContainerStatus.STATUS_FAILED);
        String a = null;
        try {
            a = b(d).trim();
        } catch (final IOException e) {
            logger.log(e);
            return cs;
        }
        // try {
        // if (a.trim().startsWith("<dlc>")) return e(d);
        // } catch (Exception e) {
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().log(java.util.logging.Level.SEVERE,
        // "Exception occured", e);
        // cs.setStatusText("DLC2 failed");
        // return cs;
        // }
        String ee = "";
        if (a.length() < 100) {
            UserIO.getInstance().requestMessageDialog(JDL.L("sys.warning.dlcerror_nocld_small", "Invalid DLC: less than 100 bytes. This cannot be a DLC"));
            cs.setStatusText(JDL.L("sys.warning.dlcerror_nocld_small", "Invalid DLC: less than 100 bytes. This cannot be a DLC"));
            return cs;
        }
        String a0 = a.substring(a.length() - 88).trim();
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info(dlcString);
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info(key + " - " + key.length());
        a = a.substring(0, a.length() - 88).trim();
        if (Encoding.filterString(a, "1234567890QAWSEDRFTGZHUJIKOLPMNBVCXY+/=qaywsxedcrfvtgbzhnujmikolp\r\n").length() != a.length()) {
            UserIO.getInstance().requestMessageDialog(JDL.L("sys.warning.dlcerror_invalid", "It seems that your dlc is not valid."));
            cs.setStatusText(JDL.L("sys.warning.dlcerror_invalid", "It seems that your dlc is not valid."));
            return cs;
        }
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info(dlcString);
        java.util.List<URL> s1;
        try {
            s1 = new ArrayList<URL>();
            s1.add(0, new URL("https://service.jdownloader.org/dlcrypt/service.php"));
            Iterator<URL> it = s1.iterator();
            while (it.hasNext()) {
                String x = null;
                URL s2 = it.next();
                try {
                    String p;
                    if (k != null && k.length == 16) {
                        /* CHECK: we should always use new String (bytes,charset) to avoid issues with system charset and utf-8 */
                        p = new String(k);
                    } else {
                        x = cs(s2, a0);
                        if (x != null && x.trim().equals("2YVhzRFdjR2dDQy9JL25aVXFjQ1RPZ")) {
                            logger.severe("You recently opened to many DLCs. Please wait a few minutes.");
                            ee += "DLC Limit reached." + " ";
                            continue;
                        }
                        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("Dec key "+decodedKey);
                        if (x == null) {
                            logger.severe("DLC Error(key): " + s2);
                            ee += s2 + "" + JDL.L("sys.warning.dlcerror_key", "DLC: Key Fehler") + " ";
                            continue;
                        }
                        // JDUtilities.getController().fireControlEvent(ControlEvent.CONTROL_INTERACTION_CALL, this);
                        p = dsk(x);
                        // String test = dsk("8dEAMOh4EcaP8QgExlHZRNeCYL9EzB3cGJIdDG2prCE=");
                        p = filterString(p);
                        if (p.length() != 16) {
                            logger.severe("DLC Error2(key): " + s2);
                            ee += s2 + "" + JDL.L("sys.warning.dlcerror_version", "DLC Fehler(1) ") + " ";
                            continue;
                        }
                        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("PLAIN KEY: " + plain);
                        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("PLAIN dlc: " + dlcString);
                        // plain="11b857cd4c4edd19";
                    }
                    String dds1 = d5(a, p);
                    dds1 = fds(dds1);
                    if (dds1 == null) {
                        logger.severe("DLC Error(xml): " + s2);
                        ee += s2 + "" + JDL.L("sys.warning.dlcerror_xml", "DLC: XML Fehler ") + " ";
                        continue;
                    }
                    dds1 = filterString(dds1);
                    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("Decr " + decryptedDlcString);
                    pxs(dds1, d);
                    /* CHECK: we should always use getBytes("UTF-8") or with wanted charset, never system charset! */
                    k = p.getBytes();
                    cs.setStatus(ContainerStatus.STATUS_FINISHED);
                    return cs;
                } catch (NoSuchAlgorithmException e) {
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_java", "DLC: Outdated Javaversion ") + e.getMessage() + " ";
                } catch (MalformedURLException e) {
                    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().log(java.util.logging.Level.SEVERE,"Exception
                    // occured",e);
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_url", "DLC: URL Fehler: ") + e.getMessage() + " ";
                } catch (IOException e) {
                    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().log(java.util.logging.Level.SEVERE,"Exception
                    // occured",e);
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_io", "DLC: Server Fehler(offline? ") + e.getMessage() + " ";
                } catch (SAXException e) {
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_version", "DLC Fehler: Veraltete JD Version (1) ") + " ";
                    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().log(java.util.logging.Level.SEVERE,"Exception
                    // occured",e);
                } catch (ParserConfigurationException e) {
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_version", "DLC Fehler: Veraltete JD Version (2)") + " ";
                    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().log(java.util.logging.Level.SEVERE,"Exception
                    // occured",e);
                } catch (Exception e) {
                    e.printStackTrace();
                    ee += s2 + "" + JDL.L("sys.warning.dlcerror_unknown", "DLC Fehler: ") + e.getMessage() + " ";
                    logger.log(e);
                }
            }
        } catch (Exception e) {
            ee += "URL Fehler " + e.getMessage() + "  ";
        }
        cs.setStatusText(JDL.L("sys.warning.dlcerror.server2", "Server claims: ") + " " + ee);
        return cs;
    }

    // private ContainerStatus e(File d4) throws SAXException, IOException,
    // ParserConfigurationException, NoSuchAlgorithmException,
    // NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
    // BadPaddingException, InstantiationException, IllegalAccessException {
    // ContainerStatus cs = new ContainerStatus(d4);
    // cs.setStatus(ContainerStatus.STATUS_FAILED);
    //
    // String ds = b(d4).trim();
    //
    // DocumentBuilderFactory f1;
    // InputSource s;
    // Document dc;
    // f1 = DocumentBuilderFactory.newInstance();
    // f1.setValidating(false);
    // f1.setIgnoringElementContentWhitespace(true);
    // f1.setIgnoringComments(true);
    // s = new InputSource(new StringReader(ds));
    // java.util.List<String> dss = new ArrayList<String>();
    // String k0, d0;
    // if (JDUtilities.getRunType() != JDUtilities.RUNTYPE_LOCAL_JARED) return
    // null;
    //
    // int d9 = 0;
    // byte[] bb = this.rjs(b3);
    //
    // for (byte b : bb) {
    // d9 += (b - d[d9]) + 1;
    // }
    // if (d9 != 16) {
    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("Wrong JD Version");
    // return null;
    // }
    // k0 = null;
    // d0 = null;
    // dc = f1.newDocumentBuilder().parse(s);
    // NodeList nds = dc.getFirstChild().getChildNodes();
    // for (int i = 0; i < nds.getLength(); i++) {
    // Node nd = nds.item(i);
    // if (nd.getNodeName().equalsIgnoreCase("services")) {
    // NodeList ss = nd.getChildNodes();
    // for (int sid = 0; sid < ss.getLength(); sid++) {
    // if (ss.item(sid).getNodeName().equalsIgnoreCase("service")) {
    // dss.add(ss.item(sid).getFirstChild().getNodeValue());
    // }
    // }
    // }
    // if (nd.getNodeName().equalsIgnoreCase("data")) {
    // k0 = nd.getAttributes().getNamedItem("k").getFirstChild().getNodeValue();
    // d0 = nd.getFirstChild().getNodeValue().trim();
    // break;
    //
    // }
    // }
    // String dk0 = null;
    // for (String s0 : dss) {
    // dk0 = cs2(s0, k0);
    // if (dk0 != null)
    // ;
    // }
    // if (dk0.contains("expired")) {
    // cs.setStatus(ContainerStatus.STATUS_FAILED);
    // cs.setStatusText("This DLC has Expired");
    // return cs;
    //
    // }
    // byte[] kkk = new byte[] { 0x51, 0x57, 0x45, 0x52, 0x54, 0x5a, 0x55, 0x49,
    // 0x4f, 0x50, 0x41, 0x53, 0x44, 0x46, 0x47, 0x48 };
    //
    // byte[] kb = new BASE64Decoder().decodeBuffer(dk0);
    // SecretKeySpec skeySpec = new SecretKeySpec(kkk, "AES");
    // Cipher dr = Cipher.getInstance("AES/ECB/NoPadding");
    // dr.init(Cipher.DECRYPT_MODE, skeySpec);
    // byte[] pgn = dr.doFinal(kb);
    //
    // String dds = d5(d0, new String(pgn));
    // dds = fds(dds);
    // dds = Encoding.filterString(dds);
    //
    // pxs(dds, d4);
    // if (dlU == null || dlU.size() == 0) {
    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().severe("No Links found: " + dss);
    //
    // return cs;
    // } else {
    //
    // cs.setStatus(ContainerStatus.STATUS_FINISHED);
    // return cs;
    // }
    // }
    // private String cs2(String s, String m) throws IOException {
    //
    // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().finer("Call " + s);
    // Browser br = new Browser();
    //
    // String rk = br.postPage(s, "dt=jdtc&st=dlc&d=" + Encoding.urlEncode(m));
    // br = null;
    // return new Regex(rk, "<rc>(.*)</rc>").getMatch(0);
    //
    // }
    // //@Override
    public String[] encrypt(String p0) {
        String b = Encoding.Base64Encode(p0);
        while (b.length() % 16 != 0) {
            b += " ";
        }
        int c7 = (int) (Math.random() * 1000000000.0);
        try {
            String rk = Hash.getMD5("" + c7).substring(0, 16);
            // randomKey = JDHash.getMD5("11").substring(0, 16);
            // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("Key: " + randomKey);
            /* CHECK: we should always use getBytes("UTF-8") or with wanted charset, never system charset! */
            byte[] k = rk.getBytes();
            // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("ENcode " + base64);
            IvParameterSpec ivSpec = new IvParameterSpec(k);
            SecretKeySpec skeySpec = new SecretKeySpec(k, "AES");
            Cipher cipher;
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            byte[] ogl = cipher.doFinal(b.getBytes());
            return new String[] { Base64.encodeToString(ogl, false), rk };
            // return new BASE64Encoder().encode(original);
        } catch (Exception e) {
            logger.log(e);
            logger.severe("DLC encryption failed (5)");
            return null;
        }
    }

    // //@Override
    public String getCoder() {
        // TODO Auto-generated method stub
        return "JD-DLC-Team";
    }

    private String b(File fu) throws IOException {
        FileReader fr = null;
        try {
            if (!fu.exists()) {
                throw new FileNotFoundException(fu.getPath());
            } else if (fu.length() == 0) {
                throw new IOException("Empty file: " + fu.getPath());
            }
            fr = new FileReader(fu);
            final BufferedReader f = new BufferedReader(fr);
            final StringBuffer bf = new StringBuffer();
            String line = null;
            while ((line = f.readLine()) != null) {
                bf.append(line + "\r\n");
            }
            f.close();
            return bf.toString();
        } finally {
            if (fr != null) {
                fr.close();
            }
        }
    }

    private String cs(URL s9, String bin) throws Exception {
        int i = 0;
        while (true) {
            i++;
            logger.finer("Call " + s9);
            Browser br = new Browser();
            br.getHeaders().put("rev", JDUtilities.getRevision());
            //
            UrlQuery qi = new UrlQuery().addAndReplace("destType", "jdtc6").addAndReplace("b", Encoding.urlEncode(UpdateController.getInstance().getAppID())).addAndReplace("srcType", "dlc").addAndReplace("data", Encoding.urlEncode(bin)).addAndReplace("v", JDUtilities.getRevision());
            br.postPage(s9 + "", qi);
            // 3f69b642cc403506ff1ee7f22b23ce40
            // new byte[]{(byte) 0xef, (byte) 0xe9, (byte) 0x0a, (byte) 0x8e,
            // (byte)
            // 0x60, (byte) 0x4a, (byte) 0x7c, (byte) 0x84, (byte) 0x0e, (byte)
            // 0x88, (byte) 0xd0, (byte) 0x3a, (byte) 0x67, (byte) 0xf6, (byte)
            // 0xb7, (byte) 0xd8};
            // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info( ri.getHtmlCode());
            // a7b3b706e3cf3770931f081926ed0d95
            if (!br.getHttpConnection().isOK() || !br.containsHTML("rc")) {
                if (i > 3) {
                    return null;
                }
            } else {
                String dk = br + "";
                String ret = new Regex(dk, "<rc>(.*)</rc>").getMatch(0);
                return ret;
            }
        }
    }

    private String dsk(String dk9) throws Exception, NoSuchAlgorithmException {
        byte[] k = gjdk();
        Thread.sleep(3000);
        @SuppressWarnings("unused")
        String key = JDHexUtils.getHexString(k);
        String str = dk9;
        byte[] j = Base64.decode(str);
        SecretKeySpec skeySpec = new SecretKeySpec(k, "AES");
        Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
        c.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] o = c.doFinal(j);
        /* CHECK: we should always use new String (bytes,charset) to avoid issues with system charset and utf-8 */
        return new String(Base64.decode(o)).substring(0, 16);
    }

    public String d5(String ct, String k7l) {
        String rte = null;
        try {
            byte[] input;
            input = Base64.decode(ct);
            byte[] k = k7l.getBytes("UTF-8");
            IvParameterSpec ivSpec = new IvParameterSpec(k);
            SecretKeySpec skeySpec = new SecretKeySpec(k, "AES");
            Cipher pl;
            byte[] opl;
            try {
                // cipher = Cipher.getInstance("AES/CBC/NoPadding");
                // cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                // original = cipher.doFinal(input);
                pl = Cipher.getInstance("AES/CBC/PKCS5Padding");
                pl.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                opl = pl.doFinal(input);
            } catch (Exception e) {
                pl = Cipher.getInstance("AES/CBC/NoPadding");
                pl.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                opl = pl.doFinal(input);
            }
            /* CHECK: we should always use new String (bytes,charset) to avoid issues with system charset and utf-8 */
            String pln = new String(Base64.decode(opl));
            rte = pln;
        } catch (Exception e) {
            logger.log(e);
        }
        if (rte == null || rte.indexOf("<content") < 0) {
            logger.info("Old DLC Version");
            try {
                byte[] ii;
                ii = Base64.decode(ct);
                /* CHECK: we should always use getBytes("UTF-8") or with wanted charset, never system charset! */
                byte[] k = k7l.getBytes();
                SecretKeySpec skeySpec = new SecretKeySpec(k, "AES");
                Cipher cp;
                byte[] gln;
                // Alte DLC Container sind mit PKCS5Padding verchlüsseöt.- neue
                // ohne
                // padding
                try {
                    cp = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cp.init(Cipher.DECRYPT_MODE, skeySpec);
                    gln = cp.doFinal(ii);
                } catch (Exception e) {
                    cp = Cipher.getInstance("AES/ECB/NoPadding");
                    cp.init(Cipher.DECRYPT_MODE, skeySpec);
                    gln = cp.doFinal(ii);
                }
                String po = new String(Base64.decode(gln));
                rte = po;
            } catch (Exception e) {
                logger.log(e);
                rte = null;
            }
        }
        if (rte == null) {
            logger.severe("DLC Decryption failed (3)");
        }
        return rte;
    }

    private String fds(String dcs) {
        if (dcs == null) {
            return dcs;
        }
        dcs = dcs.trim();
        if (Regex.matches(dcs, "<dlc>(.*)<\\/dlc>")) {
            dcs = "<dlc>" + new Regex(dcs, "<dlc>(.*)<\\/dlc>").getMatch(0).trim() + "</dlc>";
        }
        return dcs;
    }

    private void pdx1(Node n, File d) throws InstantiationException, IllegalAccessException {
        /*
         * Original Release Version. In der XMl werden plantexte verwendet
         */
        cls = new ArrayList<CrawledLink>();
        CrawledLink nl;
        int c = 0;
        NodeList ps = n.getChildNodes();
        for (int pcs = 0; pcs < ps.getLength(); pcs++) {
            ps.item(pcs).getAttributes().getNamedItem("name").getNodeValue();
            NodeList uls = ps.item(pcs).getChildNodes();
            for (int fc = 0; fc < uls.getLength(); fc++) {
                Node f = uls.item(fc);
                if (f != null) {
                    NodeList data = f.getChildNodes();
                    java.util.List<String> ls = new ArrayList<String>();
                    java.util.List<String> pws = new ArrayList<String>();
                    String pc = "";
                    for (int entry = 0; entry < data.getLength(); entry++) {
                        final String nodeName = data.item(entry).getNodeName();
                        if ("url".equalsIgnoreCase(nodeName)) {
                            ls.add(data.item(entry).getTextContent());
                        } else if ("password".equalsIgnoreCase(nodeName)) {
                            List<String> ret = parsePassword(data.item(entry).getTextContent());
                            for (String pw : ret) {
                                if (!pws.contains(pw)) {
                                    pws.add(pw);
                                }
                            }
                        } else if ("comment".equalsIgnoreCase(nodeName)) {
                            final String cmt = data.item(entry).getTextContent();
                            if (pc.indexOf(cmt) < 0) {
                                pc += cmt + " | ";
                            }
                        }
                    }
                    if (pc.length() > 0) {
                        pc = pc.substring(0, pc.length() - 3);
                    }
                    final String comment = "from Container: " + d + " : " + pc;
                    for (int lcs = 0; lcs < ls.size(); lcs++) {
                        // // PluginForHost pHost =
                        // findHostPlugin(links.get(linkCounter));
                        // if (pHost != null) {
                        // newLink = new CrawledLink((PluginForHost)
                        // pHost.getClass().newInstance(),
                        // links.get(linkCounter).substring(links.get(linkCounter
                        // ).lastIndexOf("/")
                        // + 1), pHost.getHost(), null, true);
                        nl = new CrawledLink(ls.get(lcs));
                        if (pws != null && pws.size() > 0) {
                            nl.getArchiveInfo().getExtractionPasswords().addAll(pws);
                        }
                        final PackageInfo dpi = new PackageInfo();
                        nl.setDesiredPackageInfo(dpi);
                        cls.add(nl);
                        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info(""+links.get(linkCounter));
                        c++;
                        // }
                    }
                }
            }
        }
    }

    private void pdx2(Node node, File d) throws InstantiationException, IllegalAccessException {
        /*
         * Neue Version. Alle user generated inhalte werden jetzt base64 verschlüsselt abgelegt. nur generator nicht
         */
        cls = new ArrayList<CrawledLink>();
        CrawledLink nl;
        ;
        int c = 0;
        NodeList ps = node.getChildNodes();
        for (int pc = 0; pc < ps.getLength(); pc++) {
            if (!ps.item(pc).getNodeName().equals("package")) {
                continue;
            }
            ps.item(pc).getAttributes().getNamedItem("name").getNodeValue();
            NodeList uls = ps.item(pc).getChildNodes();
            for (int fc = 0; fc < uls.getLength(); fc++) {
                Node file = uls.item(fc);
                if (file != null) {
                    NodeList data = file.getChildNodes();
                    List<String> ls = new ArrayList<String>();
                    List<String> pws = new ArrayList<String>();
                    String pgc = "";
                    for (int entry = 0; entry < data.getLength(); entry++) {
                        final String nodeName = data.item(entry).getNodeName();
                        if ("url".equalsIgnoreCase(nodeName)) {
                            ls.add(Encoding.Base64Decode(data.item(entry).getTextContent()));
                        } else if ("password".equalsIgnoreCase(nodeName)) {
                            final List<String> ret = parsePassword(Encoding.Base64Decode(data.item(entry).getTextContent()));
                            for (String pw : ret) {
                                if (!pws.contains(pw)) {
                                    pws.add(pw);
                                }
                            }
                        } else if ("comment".equalsIgnoreCase(nodeName)) {
                            final String cmt = Encoding.Base64Decode(data.item(entry).getTextContent());
                            if (pgc.indexOf(cmt) < 0) {
                                pgc += cmt + " | ";
                            }
                        }
                    }
                    if (pgc.length() > 0) {
                        pgc = pgc.substring(0, pgc.length() - 3);
                    }
                    final String comment = "from Container: " + d + " : " + pc;
                    for (int lc = 0; lc < ls.size(); lc++) {
                        // PluginForHost pHost =
                        // findHostPlugin(links.get(linkCounter));
                        // if (pHost != null) {
                        // newLink = new CrawledLink((PluginForHost)
                        // pHost.getClass().newInstance(),
                        // links.get(linkCounter).substring(links.get(linkCounter
                        // ).lastIndexOf("/")
                        // + 1), pHost.getHost(), null, true);
                        nl = new CrawledLink(ls.get(lc));
                        if (pws != null && pws.size() > 0) {
                            nl.getArchiveInfo().getExtractionPasswords().addAll(pws);
                        }
                        final PackageInfo dpi = new PackageInfo();
                        nl.setDesiredPackageInfo(dpi);
                        cls.add(nl);
                        c++;
                    }
                }
            }
        }
    }

    private static java.util.List<String> parsePassword(String password) {
        java.util.List<String> pws = new ArrayList<String>();
        if (password == null || password.length() == 0 || password.matches("[\\s]*")) {
            return pws;
        }
        if (password.matches("[\\s]*\\{[\\s]*\".*\"[\\s]*\\}[\\s]*$")) {
            password = password.replaceFirst("[\\s]*\\{[\\s]*\"", "").replaceFirst("\"[\\s]*\\}[\\s]*$", "");
            for (String pw : password.split("\"[\\s]*\\,[\\s]*\"")) {
                if (!pws.contains(pw)) {
                    pws.add(pw);
                }
            }
        }
        if (pws.size() == 0) {
            pws.add(password);
        }
        return pws;
    }

    private void pcx3(Node node, File dlc) throws InstantiationException, IllegalAccessException {
        /*
         * alle inhalte sind base64 verschlüsselt. XML Str5uktur wurde angepasst
         */
        logger.info("Parse v3");
        cls = new ArrayList<CrawledLink>();
        NodeList ps = node.getChildNodes();
        for (int pgs = 0; pgs < ps.getLength(); pgs++) {
            if (!ps.item(pgs).getNodeName().equals("package")) {
                continue;
            }
            final PackageInfo dpi = new PackageInfo();
            String pn = Encoding.Base64Decode(ps.item(pgs).getAttributes().getNamedItem("name").getNodeValue());
            String oos = ps.item(pgs).getAttributes().getNamedItem("passwords") == null ? null : Encoding.Base64Decode(ps.item(pgs).getAttributes().getNamedItem("passwords").getNodeValue());
            String cs2 = ps.item(pgs).getAttributes().getNamedItem("comment") == null ? null : Encoding.Base64Decode(ps.item(pgs).getAttributes().getNamedItem("comment").getNodeValue());
            String ca3 = ps.item(pgs).getAttributes().getNamedItem("category") == null ? null : Encoding.Base64Decode(ps.item(pgs).getAttributes().getNamedItem("category").getNodeValue());
            if (pn != null && !"n.A.".equals(pn)) {
                // n.A. is no good default packageName
                dpi.setName(LinknameCleaner.cleanFileName(pn, false, true, LinknameCleaner.EXTENSION_SETTINGS.KEEP, false));
            }
            if (ca3 != null && ca3.trim().length() > 0) {
                // dpi.setComment("[" + ca3 + "] " + cs2);
            } else {
                // dpi.setComment(cs2);
            }
            NodeList urls = ps.item(pgs).getChildNodes();
            for (int fileCounter = 0; fileCounter < urls.getLength(); fileCounter++) {
                Node file = urls.item(fileCounter);
                if (file != null) {
                    NodeList data = file.getChildNodes();
                    java.util.List<String> ls2 = new ArrayList<String>();
                    java.util.List<String> n5 = new ArrayList<String>();
                    for (int entry = 0; entry < data.getLength(); entry++) {
                        final String nodeName = data.item(entry).getNodeName();
                        if ("url".equalsIgnoreCase(nodeName)) {
                            String sls = Encoding.Base64Decode(data.item(entry).getTextContent());
                            String[] lsr = HTMLParser.getHttpLinks(sls, null);
                            final boolean none = lsr.length == 0;
                            if (none && StringUtils.isNotEmpty(sls)) {
                                lsr = Regex.getLines(sls);
                            }
                            if (none) {
                                while (true) {
                                    logger.warning("Failed DLC Decoding. Try to decode again");
                                    String old = sls;
                                    sls = Encoding.Base64Decode(sls);
                                    if (old.equals(sls)) {
                                        break;
                                    }
                                }
                            }
                            // workaround. we accidently stored wrong youtube links (pluginpatternmatcher instead of contentUrl in the dlcs.
                            if (HTMLParser.getProtocol(sls) != null) {
                                ls2.add(sls);
                            }
                            for (String link : lsr) {
                                if (link.startsWith("youtubev2://")) {
                                    String[] ytInfo = new Regex(link, "youtubev2\\:\\/\\/(.*?)/(.*?)/").getRow(0);
                                    if (ytInfo != null && ytInfo.length >= 2) {
                                        // convert to decrypter link
                                        link = "http://www.youtube.com/watch?v=" + ytInfo[1] + "&variant=" + ytInfo[0];
                                    }
                                }
                                if (!sls.trim().equals(link.trim())) {
                                    ls2.add(link);
                                }
                            }
                            if (lsr.length > 1) {
                                logger.severe("DLC Error. Generator Link split Error");
                                break;
                            }
                        } else if ("filename".equals(nodeName)) {
                            n5.add(Encoding.Base64Decode(data.item(entry).getTextContent()));
                        }
                    }
                    while (ls2.size() > n5.size()) {
                        n5.add(null);
                    }
                    final List<String> pws = parsePassword(oos);
                    for (int lcs = 0; lcs < ls2.size(); lcs++) {
                        final CrawledLink nl = new CrawledLink(ls2.get(lcs));
                        if (pws != null && pws.size() > 0) {
                            nl.getArchiveInfo().getExtractionPasswords().addAll(pws);
                        }
                        nl.setDesiredPackageInfo(dpi.getCopy());
                        final String name = n5.get(lcs);
                        if (StringUtils.isNotEmpty(name)) {
                            final String ext = Files.getExtension(name);
                            if (ext != null) {
                                final ExtensionsFilterInterface extension = CompiledFiletypeFilter.getExtensionsFilterInterface(ext);
                                if (extension != null) {
                                    nl.setName(name);
                                }
                            }
                        }
                        cls.add(nl);
                    }
                }
            }
        }
    }

    private void pxs(String cs, File dlc) throws SAXException, IOException, ParserConfigurationException, InstantiationException, IllegalAccessException {
        DocumentBuilderFactory f;
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info(jdtc);
        InputSource is;
        Document doc;
        f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        // www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("encrypted: "+dlcString);
        if (cs.trim().startsWith("<dlc")) {
            // New (cbc Änderung)
            is = new InputSource(new StringReader(cs));
        } else {
            // alt
            is = new InputSource(new StringReader("<dlc>" + cs + "</dlc>"));
        }
        doc = f.newDocumentBuilder().parse(is);
        NodeList nodes = doc.getFirstChild().getChildNodes();
        header = new HashMap<String, String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase("header")) {
                NodeList entries = node.getChildNodes();
                for (int entryCounter = 0; entryCounter < entries.getLength(); entryCounter++) {
                    if (entries.item(entryCounter).getNodeName().equalsIgnoreCase("generator")) {
                        NodeList generatorEntries = entries.item(entryCounter).getChildNodes();
                        for (int genCounter = 0; genCounter < generatorEntries.getLength(); genCounter++) {
                            if (generatorEntries.item(genCounter).getNodeName().equalsIgnoreCase("app")) {
                                header.put("generator.app", Encoding.Base64Decode(generatorEntries.item(genCounter).getTextContent()));
                            }
                            if (generatorEntries.item(genCounter).getNodeName().equalsIgnoreCase("version")) {
                                header.put("generator.version", Encoding.Base64Decode(generatorEntries.item(genCounter).getTextContent()));
                            }
                            if (generatorEntries.item(genCounter).getNodeName().equalsIgnoreCase("url")) {
                                header.put("generator.url", Encoding.Base64Decode(generatorEntries.item(genCounter).getTextContent()));
                            }
                        }
                    }
                    if (entries.item(entryCounter).getNodeName().equalsIgnoreCase("tribute")) {
                        NodeList names = entries.item(entryCounter).getChildNodes();
                        String tribute = "";
                        for (int tributeCounter = 0; tributeCounter < names.getLength(); tributeCounter++) {
                            tribute += Encoding.Base64Decode(names.item(tributeCounter).getTextContent());
                            if (tributeCounter + 1 < names.getLength()) {
                                tribute += ", ";
                            }
                        }
                        header.put("tribute", tribute);
                    }
                    if (entries.item(entryCounter).getNodeName().equalsIgnoreCase("dlcxmlversion")) {
                        String dlcXMLVersion = Encoding.Base64Decode(entries.item(entryCounter).getTextContent());
                        header.put("dlcxmlversion", dlcXMLVersion);
                    }
                }
            }
            if (node.getNodeName().equalsIgnoreCase("content")) {
                logger.info("dlcxmlversion: " + header.get("dlcxmlversion"));
                if (header.containsKey("dlcxmlversion") && header.get("dlcxmlversion") != null && header.get("dlcxmlversion").equals("20_02_2008")) {
                    pcx3(node, dlc);
                } else if (header.containsKey("generator.app") && header.get("generator.app").equals("jDownloader") && header.containsKey("generator.version") && Double.parseDouble(header.get("generator.version")) < 654.0) {
                    pdx1(node, dlc);
                } else {
                    pdx2(node, dlc);
                }
            }
            // org.appwork.utils.logging2.extmanager.LoggerFactory.getDefaultLogger().info("Header " + header);
        }
    }

    private byte[] gjdk() {
        byte[] k = gk();
        @SuppressWarnings("unused")
        /* CHECK: we should always use new String (bytes,charset) to avoid issues with system charset and utf-8 */
        String h = new String(k);
        // doit
        return k;
    }

    public static byte[] base16Decode(String code) {
        while (code.length() % 2 > 0) {
            code += "0";
        }
        final byte[] res = new byte[code.length() / 2];
        int i = 0;
        while (i < code.length()) {
            res[i / 2] = (byte) Integer.parseInt(code.substring(i, i + 2), 16);
            i += 2;
        }
        return res;
    }

    private byte[] gk() {
        return base16Decode("447e787351e60e2c6a96b3964be0c9bd");
    }

    public static String xmltoStr(Document header) {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(header);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            return xmlString;
        } catch (Exception e) {
            LogController.CL().log(e);
        }
        return null;
    }

    public java.util.List<DownloadLink> getPackageFiles(FilePackage filePackage, List<DownloadLink> links) {
        java.util.List<DownloadLink> ret = new ArrayList<DownloadLink>();
        // ret.add(DownloadLink);
        Iterator<DownloadLink> iterator = links.iterator();
        DownloadLink nextDownloadLink = null;
        while (iterator.hasNext()) {
            nextDownloadLink = iterator.next();
            if (filePackage == nextDownloadLink.getFilePackage()) {
                ret.add(nextDownloadLink);
            }
        }
        return ret;
    }

    protected String createContainerStringByLinks(List<? extends AbstractPackageChildrenNode<?>> links) {
        final List<AbstractPackageChildrenNode<?>> nodes = new ArrayList<AbstractPackageChildrenNode<?>>(links);
        final HashSet<String> urls = new HashSet<String>();
        // filter
        final Iterator<? extends AbstractPackageChildrenNode<?>> it = nodes.iterator();
        while (it.hasNext()) {
            final AbstractPackageChildrenNode<?> node = it.next();
            final DownloadLink downloadLink;
            if (node instanceof DownloadLink) {
                downloadLink = (DownloadLink) node;
            } else if (node instanceof CrawledLink) {
                downloadLink = ((CrawledLink) node).getDownloadLink();
            } else {
                it.remove();
                continue;
            }
            final String url = downloadLink.getDefaultPlugin().buildContainerDownloadURL(downloadLink, downloadLink.getDefaultPlugin());
            if (url == null || !urls.add(url)) {
                it.remove();
                continue;
            }
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        SubConfiguration cfg = SubConfiguration.getConfig("DLCCONFIG");
        InputSource inSourceHeader = new InputSource(new StringReader("<header><generator><app></app><version/><url></url></generator><tribute/><dlcxmlversion/></header>"));
        InputSource inSourceContent = new InputSource(new StringReader("<content/>"));
        try {
            Document content = factory.newDocumentBuilder().parse(inSourceContent);
            Document header = factory.newDocumentBuilder().parse(inSourceHeader);
            Node header_generator_app = header.getFirstChild().getFirstChild().getChildNodes().item(0);
            Node header_generator_version = header.getFirstChild().getFirstChild().getChildNodes().item(1);
            Node header_generator_url = header.getFirstChild().getFirstChild().getChildNodes().item(2);
            header_generator_app.appendChild(header.createTextNode(Encoding.Base64Encode("JDownloader")));
            header_generator_version.appendChild(header.createTextNode(Encoding.Base64Encode(JDUtilities.getRevision())));
            header_generator_url.appendChild(header.createTextNode(Encoding.Base64Encode("http://jdownloader.org")));
            Node header_tribute = header.getFirstChild().getChildNodes().item(1);
            if (cfg.getBooleanProperty("ASK_ADD_INFOS", false)) {
                Element element = header.createElement("name");
                header_tribute.appendChild(element);
                element.appendChild(header.createTextNode(Encoding.Base64Encode(UserIO.getInstance().requestInputDialog("Uploader Name"))));
            }
            if (cfg.getStringProperty("UPLOADERNAME", null) != null && cfg.getStringProperty("UPLOADERNAME", null).trim().length() > 0) {
                Element element = header.createElement("name");
                header_tribute.appendChild(element);
                element.appendChild(header.createTextNode(Encoding.Base64Encode(cfg.getStringProperty("UPLOADERNAME", null))));
            }
            Node header_dlxxmlversion = header.getFirstChild().getChildNodes().item(2);
            header_dlxxmlversion.appendChild(header.createTextNode(Encoding.Base64Encode("20_02_2008")));
            final List<Object> packages = new ArrayList<Object>();
            final HashMap<Object, List<AbstractPackageChildrenNode>> packageNodesMap = new HashMap<Object, List<AbstractPackageChildrenNode>>();
            for (int i = 0; i < nodes.size(); i++) {
                if (!packages.contains(nodes.get(i).getParentNode())) {
                    packages.add(nodes.get(i).getParentNode());
                }
                List<AbstractPackageChildrenNode> list = packageNodesMap.get(nodes.get(i).getParentNode());
                if (list == null) {
                    list = new ArrayList<AbstractPackageChildrenNode>();
                    packageNodesMap.put(nodes.get(i).getParentNode(), list);
                }
                list.add(nodes.get(i));
            }
            for (final Object pkg : packages) {
                final Element pkgElement = content.createElement("package");
                if (pkg == null) {
                    pkgElement.setAttribute("name", Encoding.Base64Encode("various"));
                } else {
                    final String name;
                    final String comment;
                    if (pkg instanceof CrawledPackage) {
                        name = ((CrawledPackage) pkg).getName();
                        comment = ((CrawledPackage) pkg).getComment();
                    } else if (pkg instanceof FilePackage) {
                        name = ((FilePackage) pkg).getName();
                        comment = ((FilePackage) pkg).getComment();
                    } else {
                        continue;
                    }
                    pkgElement.setAttribute("name", Encoding.Base64Encode(name));
                    pkgElement.setAttribute("comment", Encoding.Base64Encode(comment));
                    String category = Encoding.Base64Encode("various");
                    if (cfg.getBooleanProperty("ASK_ADD_INFOS", false)) {
                        category = Encoding.Base64Encode(UserIO.getInstance().requestInputDialog("Category for package " + name));
                    }
                    pkgElement.setAttribute("category", category);
                }
                content.getFirstChild().appendChild(pkgElement);
                final List<AbstractPackageChildrenNode> packageNodes = packageNodesMap.get(pkg);
                for (AbstractPackageChildrenNode node : packageNodes) {
                    final DownloadLink downloadLink;
                    if (node instanceof DownloadLink) {
                        downloadLink = (DownloadLink) node;
                    } else if (node instanceof CrawledLink) {
                        downloadLink = ((CrawledLink) node).getDownloadLink();
                    } else {
                        continue;
                    }
                    pkgElement.appendChild(content.createElement("file"));
                    final Element urlElement = content.createElement("url");
                    // the contenturl always should direct to exactly the downloadlink that is exported.
                    if (downloadLink.getContentUrl() != null) {
                        urlElement.appendChild(content.createTextNode(Encoding.Base64Encode(downloadLink.getContentUrl())));
                    } else {
                        urlElement.appendChild(content.createTextNode(Encoding.Base64Encode(downloadLink.getPluginPatternMatcher())));
                    }
                    final Element fileNameElement = content.createElement("filename");
                    fileNameElement.appendChild(content.createTextNode(Encoding.Base64Encode(downloadLink.getName())));
                    final Element fileSizeElement = content.createElement("size");
                    fileSizeElement.appendChild(content.createTextNode(Encoding.Base64Encode(String.valueOf(Math.max(0, downloadLink.getKnownDownloadSize())))));
                    pkgElement.getLastChild().appendChild(urlElement);
                    pkgElement.getLastChild().appendChild(fileNameElement);
                    pkgElement.getLastChild().appendChild(fileSizeElement);
                }
            }
            int ind1 = xmltoStr(header).indexOf("<header");
            int ind2 = xmltoStr(content).indexOf("<content");
            String ret = xmltoStr(header).substring(ind1) + xmltoStr(content).substring(ind2);
            return "<dlc>" + ret + "</dlc>";
        } catch (Exception e) {
            logger.log(e);
        }
        return null;
    }
}