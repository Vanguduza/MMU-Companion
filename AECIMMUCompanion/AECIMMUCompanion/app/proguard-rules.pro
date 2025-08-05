# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep application components
-keep class com.aeci.mmucompanion.MainActivity { *; }
-keep class com.aeci.mmucompanion.AECIMMUCompanionApplication { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Application { *; }
-keep class * extends androidx.activity.ComponentActivity { *; }

# Keep all classes annotated with @AndroidEntryPoint (Hilt)
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Apache POI keep rules
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }

# Apache XML Security
-keep class org.apache.xml.security.** { *; }
-dontwarn org.apache.xml.security.**

# XMLBeans schema types
-keep class org.apache.xmlbeans.impl.schema.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-dontwarn schemaorg_apache_xmlbeans.**

# Apache Batik SVG support for POI - suppress warnings for Java AWT classes
-dontwarn org.apache.batik.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**
-dontwarn java.awt.image.**
-dontwarn java.awt.font.**
-dontwarn java.awt.geom.**
-dontwarn java.awt.print.**
-dontwarn sun.awt.**

# OSGi framework classes used by Batik
-dontwarn org.osgi.**

# Additional POI dependencies
-keep class com.zaxxer.sparsebits.** { *; }
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# Apache Santuario XML Security
-keep class org.apache.santuario.** { *; }
-dontwarn org.apache.santuario.**

# OpenXML4J schema classes
-keep class org.openxmlformats.schemas.** { *; }
-dontwarn org.openxmlformats.schemas.**

# Don't warn about missing optional dependencies and Java desktop classes
-dontwarn org.apache.poi.openxml4j.opc.internal.unmarshallers.**
-dontwarn org.apache.poi.ooxml.util.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.etsi.**
-dontwarn org.w3.**

# Java desktop and AWT classes not available on Android
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**
-dontwarn java.awt.image.**
-dontwarn java.awt.font.**
-dontwarn java.awt.geom.**
-dontwarn java.awt.print.**
-dontwarn java.beans.**
-dontwarn javax.print.**
-dontwarn sun.awt.**
-dontwarn sun.java2d.**

# PDF-related optional dependencies
-dontwarn org.apache.pdfbox.**
-dontwarn com.github.jai-imageio.**
-dontwarn org.apache.xmlgraphics.**

# JDBC classes not needed on Android
-dontwarn java.sql.**
-dontwarn javax.sql.**

# JMX classes not available on Android
-dontwarn javax.management.**

# Optional compression libraries
-dontwarn org.tukaani.**
-dontwarn com.github.luben.**

# Optional image format support
-dontwarn com.twelvemonkeys.**

# Specific XMLBeans generated classes
-dontwarn schemasMicrosoftComOfficeOffice.**
-dontwarn schemasMicrosoftComVml.**
-dontwarn schemasMicrosoftComOfficeExcel.**
-dontwarn schemasMicrosoftComOfficeWord.**
-dontwarn org.openxmlformats.schemas.drawingml.x2006.main.**
-dontwarn org.openxmlformats.schemas.drawingml.x2006.chart.**
-dontwarn org.openxmlformats.schemas.drawingml.x2006.picture.**
-dontwarn org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.**
-dontwarn org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.**
-dontwarn org.openxmlformats.schemas.officeDocument.x2006.relationships.**
-dontwarn org.openxmlformats.schemas.presentationml.x2006.main.**
-dontwarn org.openxmlformats.schemas.spreadsheetml.x2006.main.**
-dontwarn org.openxmlformats.schemas.wordprocessingml.x2006.main.**

# ETSI and W3C schema classes
-dontwarn org.etsi.uri.x01903.v13.**
-dontwarn org.w3.x2000.x09.xmldsig.**

# Specific classes that R8 warns about
-dontwarn org.apache.xmlbeans.impl.xb.xsdschema.**
-dontwarn org.apache.xmlbeans.impl.xb.xmlconfig.**
-dontwarn org.apache.xmlbeans.impl.jam.**
-dontwarn org.apache.xmlbeans.impl.common.**
-dontwarn org.apache.xmlbeans.impl.tool.**
-dontwarn org.apache.xmlbeans.impl.validator.**

# iText PDF library
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Bouncy Castle cryptography
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Reflection-based keep rules for POI
-keepclassmembers class * {
    @org.apache.poi.util.Internal *;
}

# Keep all enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile