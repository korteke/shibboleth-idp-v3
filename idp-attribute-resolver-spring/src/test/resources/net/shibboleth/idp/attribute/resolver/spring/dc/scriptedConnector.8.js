IdPAttribute = Java.type("net.shibboleth.idp.attribute.IdPAttribute");
StringAttributeValue = Java.type("net.shibboleth.idp.attribute.StringAttributeValue");
HashSet = Java.type("java.util.HashSet");
Integer = Java.type("java.lang.Integer");

attr = new IdPAttribute("ScriptedOne");
set = new HashSet(2);
set.add(new StringAttributeValue("Value 1"));
set.add(new StringAttributeValue("Value 2"));
attr.setValues(set);
connectorResults.add(attr);

attr = new IdPAttribute("TwoScripted");
set = new HashSet(4);
set.add(new StringAttributeValue("1Value"));
set.add(new StringAttributeValue("2Value"));
set.add(new Integer(4));
set.add(new StringAttributeValue("3Value"));
attr.setValues(set);
connectorResults.add(attr);

connectorResults.add(new Integer(4));
