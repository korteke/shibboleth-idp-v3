' c:\program file\foo\bar/idp/ -> c:/program file/foo/bar/idp
newHome = Replace(Session.Property("INSTALLDIR"), "\", "/")
if (Right(newHome, 1) = "/") then
    Session.Property("JAVA_IDP_HOME") = Left(newHome, Len(newHome) -1)
else 
    Session.Property("JAVA_IDP_HOME") = newHome
end if
