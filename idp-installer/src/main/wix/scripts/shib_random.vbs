
'Set WshShell = CreateObject("WScript.Shell")
'WshShell.popup("Hello world")

Set TypeLib = CreateObject("Scriptlet.TypeLib")
JettyPassword=left(TypeLib.Guid, 38)
' Prefix the property with the MM GUID....
Session.Property("JETTY_PASS.684B3207_0D64_43E6_9E6A_3ACB8B7672D7") = JettyPassword
