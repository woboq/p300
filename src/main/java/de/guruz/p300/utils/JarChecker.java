/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.guruz.p300.utils;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Can check the signature of the files in a jar. 
 * 
 * Based on code from SUN
 * 
 * @author guruz
 *
 */
public class JarChecker {

	public static X509Certificate getOurCert() throws Exception {
		// it is correct to take this class loader here!
		InputStream inStream = /*Thread.currentThread().getContextClassLoader().*/ JarChecker.class.getResourceAsStream("/de/guruz/p300/certificate");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf
				.generateCertificate(inStream);
		inStream.close();

		return cert;

	}

	public static boolean checkSignature(File f) {
		try {
			// get our cert
			X509Certificate ourCert = JarChecker.getOurCert();
			//System.out.println(ourCert.toString());
			
			URL u = f.toURI().toURL();
			URL ju = new URL("jar:" + u.toExternalForm() + "!/");
			JarURLConnection jc = (JarURLConnection) ju.openConnection();
			JarFile jf = jc.getJarFile();

			// Ensure there is a manifest file
			Manifest man = jf.getManifest();
			if (man == null) {
				throw new SecurityException("No manifest");
			}

			// Ensure all the entries' signatures verify correctly
			byte[] buffer = new byte[8192];
			Enumeration<JarEntry> entries = jf.entries();
			Vector<JarEntry> entriesVec = new Vector<JarEntry>();

			while (entries.hasMoreElements()) {
				JarEntry je = entries.nextElement();
				entriesVec.addElement(je);
				InputStream is = jf.getInputStream(je);
				int n = 0;
				while (n != -1) {
					// we just read. this will throw a SecurityException
					// if a signature/digest check fails.
					n = is.read(buffer, 0, buffer.length);
				}
				is.close();
			}
			//jf.close();

			// Get the list of signer certificates
			Enumeration<JarEntry> e = entriesVec.elements();
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();

				if (je.isDirectory()) {
					continue;
				}
				// Every file must be signed - except
				// files in META-INF
				java.security.cert.Certificate[] certs = je.getCertificates();
				if ((certs == null) || (certs.length == 0)) {
					if (!je.getName().startsWith("META-INF")) {
						throw new SecurityException("Unsigned files!");
					}
				} else {
					java.security.cert.Certificate cert = certs[0];

					if (cert instanceof X509Certificate) {
						X509Certificate x509cert = (X509Certificate) cert;
						
						if (!x509cert.equals (ourCert)) {
							throw new SecurityException ("Wrongly signed files!");
						}
						
						
						// used by guruz to get encoded version of ours ;)
						//System.out.println(cert.getClass());
						//byte[] encoded = x509cert.getEncoded();

						//FileOutputStream fos = new FileOutputStream(new File(
						//		"/tmp/cert.txt"));
						//fos.write(encoded);
						//fos.close();
					} else {
						return false;
					}
				}
			}

		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println (e.getMessage());
			return false;
		}
		return true;
	}
}
