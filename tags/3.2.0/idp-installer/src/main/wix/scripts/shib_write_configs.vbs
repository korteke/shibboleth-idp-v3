'
' Code taken from the Shib SP install
'
Dim FileSystemObj, AntFile, PropsFile, JettyFile, JettyAntFile, LogFile
Dim CustomData, msiProperties, InstallDir, IdPScope, DebugInstall
Dim ConfigureAd, AdDomain, AdUser, AdPass, AdUseGC, LDAPFile, LDAPPort
Dim LDAPSearchPath

Set FileSystemObj = CreateObject("Scripting.FileSystemObject")

' Eek 
on error resume next

'Get the Parameters values via CustomActionData
CustomData = Session.Property("CustomActionData")
msiProperties = split(CustomData,";@;")
InstallDir = msiProperties(0)

'Remove all trailing backslashes to normalize
do while (mid(InstallDir,Len(InstallDir),1) = "\")
  InstallDir = mid(InstallDir,1,Len(InstallDir)-1)
loop
set LogFile=FileSystemObj.OpenTextFile(InstallDir & "\idp_installation.Log" , 2, True)

InstallDirJava = Replace(InstallDir, "\", "/")
InstallDirWindows = Replace(InstallDirJava, "/", "\\")

IdPHostName = LCase(msiProperties(1))
InstallJetty = LCase(msiProperties(2))
IdPScope = LCase(msiProperties(3))
DebugInstall = LCase(msiProperties(4))
ConfigureAd = LCase(msiProperties(5))
if ConfigureAd = "true" then
   AdDomain = LCase(msiProperties(6))
   AdUser = LCase(msiProperties(7))
   AdPass = LCase(msiProperties(8))
   AdUseGC = LCase(msiProperties(9))
end if

LogFile.WriteLine "Installing to " & InstallDirJava
LogFile.WriteLine "Host " & IdPHostName
LogFile.WriteLine "IntallJetty" & InstallJetty

KeyStorePassword=left(CreateObject("Scriptlet.TypeLib").Guid, 38)
SealerPassword=left(CreateObject("Scriptlet.TypeLib").Guid, 38)
SsoStorePassword=left(CreateObject("Scriptlet.TypeLib").Guid, 38)

set AntFile=FileSystemObj.OpenTextFile(InstallDir & "\idp.install.properties" , 2, True)
if (Err.Number = 0 ) then
    AntFile.WriteLine "#"
    AntFile.WriteLine "# File with properties for ANT"
    AntFile.WriteLine "#"
    AntFile.WriteLine "idp.noprompt=yes"
    AntFile.WriteLine "idp.host.name=" & IdpHostName
    AntFile.WriteLine "idp.uri.subject.alt.name=https://" & IdpHostName & "/idp/shibboleth"
    AntFile.WriteLine "idp.keystore.password=" & KeyStorePassword
    AntFile.WriteLine "idp.sealer.password=" & SealerPassword
    AntFile.WriteLine "idp.target.dir=" & InstallDirJava 
    AntFile.WriteLine "idp.merge.properties=idp.install.replace.properties"
    if (IdPScope <> "") then
       AntFile.WriteLine "idp.scope=" & IdPScope
    end if
    if ConfigureAd = "true" then
       AntFile.WriteLine "ldap.merge.properties=ldap.mergeProperties"
    end if
    AntFile.WriteLine "#"
    AntFile.WriteLine "# Debug"
    AntFile.WriteLine "#"
    if (DebugInstall <> "") then
        AntFile.WriteLine "idp.no.tidy=true"
    else
        AntFile.WriteLine "#idp.no.tidy=true"
    end if
    AntFile.Close
end if

set PropsFile=FileSystemObj.OpenTextFile(InstallDir & "\idp.install.replace.properties" , 2, True)
if (Err.Number = 0 ) then
    PropsFile.WriteLine "#"
    PropsFile.WriteLine "# File to be merged into idp.properties"
    PropsFile.WriteLine "#"
    PropsFile.WriteLine "idp.entityID=https://" & IdpHostName & "/idp/shibboleth"
    PropsFile.WriteLine "idp.sealer.storePassword=" & SealerPassword
    PropsFile.WriteLine "idp.sealer.keyPassword=" & SealerPassword
    if (IdPScope <> "") then
        PropsFile.WriteLine "idp.scope=" & IdPScope
    end if
    PropsFile.Close
else
    LogFile.Writeline "PropsFile failed " & Err & "  -  " & PropsFile
end if

if (InstallJetty <> "") then
    set JettyAntFile=FileSystemObj.OpenTextFile(InstallDir & "\jetty.install.properties" , 2, True)
    if (Err.Number = 0 ) then
	JettyAntFile.WriteLine "#"
	JettyAntFile.WriteLine "# File with properties for ANT"
	JettyAntFile.WriteLine "#"
	JettyAntFile.WriteLine "jetty.merge.properties="& InstallDirJava & "/jetty.install.replace.properties"
	JettyAntFile.WriteLine "idp.host.name=" & IdpHostName
	JettyAntFile.WriteLine "idp.keystore.password=" & SsoStorePassword
	JettyAntFile.WriteLine "idp.uri.subject.alt.name=https://" & IdpHostName & "/idp/shibboleth"
	JettyAntFile.WriteLine "idp.target.dir=" & InstallDirJava 
        if (DebugInstall <> "") then
	    JettyAntFile.WriteLine "jetty.no.tidy=true"
	else 
	    JettyAntFile.WriteLine "#jetty.no.tidy=true"
	end if
	JettyAntFile.Close
    else
	LogFile.Writeline "jettyAnt failed " & Err
    end if

    set JettyFile=FileSystemObj.OpenTextFile(InstallDir & "\jetty.install.replace.properties" , 2, True)
    if (Err.Number = 0 ) then
	JettyFile.WriteLine "#"
	JettyFile.WriteLine "# File to be merged into jetty's idp.ini file"
	JettyFile.WriteLine "#"

	JettyFile.WriteLine "jetty.host=0.0.0.0"
	JettyFile.WriteLine "jetty.https.port=443"
	JettyFile.WriteLine "jetty.backchannel.port=8443"
	JettyFile.WriteLine "jetty.backchannel.keystore.path=" & InstallDirJava & "/credentials/idp-backchannel.p12"
	JettyFile.WriteLine "jetty.browser.keystore.path=" & InstallDirJava & "/credentials/idp-userfacing.p12"
	JettyFile.WriteLine "jetty.backchannel.keystore.password=" & KeyStorePassword
	JettyFile.WriteLine "jetty.browser.keystore.password=" & SsoStorePassword
	JettyFile.WriteLine "jetty.backchannel.keystore.type=PKCS12"
	JettyFile.WriteLine "jetty.browser.keystore.type=PKCS12"
	JettyFile.WriteLine "jetty.war.path=" & InstallDirJava & "/war/idp.war"
	JettyFile.WriteLine "jetty.jaas.path=" & InstallDirJava & "/conf/authn/jaas.config"
	JettyFile.WriteLine "jetty.nonhttps.host=localhost"
	JettyFile.WriteLine "jetty.nonhttps.port=80"

	JettyFile.Close
    else
	LogFile.Writeline "jetty failed " & Err
    end if
else
   LogFile.WriteLine "NoJetty " & InstallJetty
end if

if ConfigureAd = "true" then

    if AdUseGc= "true" then
        LDAPPort="3268"
        LDAPSearchPath="DC=" &Replace(AdDomain, ".", ", DC=")
    else
        LDAPPort="389"
        LDAPSearchPath="CN=Users, DC=" &Replace(AdDomain, ".", ", DC=")
    end if

    set LDAPFile=FileSystemObj.OpenTextFile(InstallDir & "\ldap.mergeProperties" , 2, True)
    if (Err.Number = 0 ) then
        LDAPFile.Writeline "idp.authn.LDAP.authenticator= adAuthenticator"
        LDAPFile.Writeline "idp.authn.LDAP.ldapURL=ldap://" & AdDomain & ":" & LDAPPort
        LDAPFile.Writeline "idp.authn.LDAP.baseDN=" & LDAPSearchPath
        LDAPFile.Writeline "idp.authn.LDAP.userFilter= (sAMAccountName={user})"
        LDAPFile.Writeline "idp.authn.LDAP.bindDN=" & AdUser & "@" & AdDomain
        LDAPFile.Writeline "idp.authn.LDAP.bindDNCredential=" & AdPass
        LDAPFile.Writeline "idp.authn.LDAP.dnFormat= %s@" & AdDomain
        LDAPFile.Close
    else
	LogFile.Writeline "AD Properties failed " & Err
    end if
else
   LogFile.WriteLine "NoAd " & ConfigureAd
end if


LogFile.Close