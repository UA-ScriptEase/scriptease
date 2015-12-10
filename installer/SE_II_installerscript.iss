; ScriptEase II installer script
; @author Richard Zhao

[Setup]
AppName=ScriptEase II
AppVerName=ScriptEase II
DefaultDirName={pf}\ScriptEase II
; Since no icons will be created in "{group}", we don't need the wizard                             f
; to ask for a Start Menu folder name:
DisableProgramGroupPage=yesUninstallDisplayIcon={app}\java.ico
OutputDir=.

[Files]
; all files should be listed here
Source: "ScriptEase2.jar"; DestDir: "{app}";
Source: "ScriptEase.url"; DestDir: "{app}";Source: "lib\*"; DestDir: "{app}\lib\";
Source: "lib\xstream-1.3.1\lib\*"; DestDir: "{app}\lib\xstream-1.3.1\lib\";
; Source: "translators\*"; DestDir: "{app}\translators\";
Source: "translators\nwn\*"; DestDir: "{app}\translators\nwn\";
Source: "translators\nwn\io\*"; DestDir: "{app}\translators\nwn\io\";
Source: "translators\nwn\io\genericfileformat\*"; DestDir: "{app}\translators\nwn\io\genericfileformat\";
Source: "translators\nwn\resources\*"; DestDir: "{app}\translators\nwn\resources\";
Source: "translators\nwn\resources\includes\*"; DestDir: "{app}\translators\nwn\resources\includes\";
Source: "translators\unity\*"; DestDir: "{app}\translators\unity\";
Source: "translators\unity\io\*"; DestDir: "{app}\translators\unity\io\";
Source: "translators\unity\io\constants\*"; DestDir: "{app}\translators\unity\io\constants\";
Source: "translators\unity\io\unityresource\*"; DestDir: "{app}\translators\unity\io\unityresource\";
Source: "translators\unity\lib\*"; DestDir: "{app}\translators\unity\lib\";
Source: "translators\unity\libraries\APathFinding\*"; DestDir: "{app}\translators\unity\libraries\APathFinding\";
Source: "translators\unity\libraries\ParkGameAnimation\*"; DestDir: "{app}\translators\unity\libraries\ParkGameAnimation\";
Source: "translators\unity\resources\*"; DestDir: "{app}\translators\unity\resources\";
Source: "translators\unity\resources\includes\*"; DestDir: "{app}\translators\unity\resources\includes\";
Source: "java.ico"; DestDir: "{app}";

[Dirs]
Name: "{app}\patterns"

[Icons]
; all shortcuts should be created here
Name: "{commonprograms}\ScriptEase II\ScriptEase II"; Filename: "javaw.exe"; Parameters: "-Xmx800M -jar ""{app}\ScriptEase2.jar"""; WorkingDir: "{app}"; IconFilename: "{app}\java.ico"
Name: "{commondesktop}\ScriptEase II"; Filename: "javaw.exe"; Parameters: "-Xmx800M -jar ""{app}\ScriptEase2.jar"""; WorkingDir: "{app}"; IconFilename: "{app}\java.ico"
Name: "{commonprograms}\ScriptEase II\ScriptEase on the Web"; Filename: "{app}\ScriptEase.url";

[code]

var
    NWNlocation: String;
    S: String;
    
function InitializeSetup(): Boolean;
begin
    if (RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Bioware\NWN\Neverwinter',
       'Location', NWNlocation)) = False then
    begin
      // NWN not installed
      MsgBox('Neverwinter Nights is not detected on your machine', mbInformation, MB_OK);
    end;
    Result := True;
end;


function ReplaceNWNSlash(Param: String): String;
begin

    if (RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Bioware\NWN\Neverwinter',
       'Location', NWNlocation)) = False then
      begin
        // NWN not installed
        S := 'C:/NeverwinterNights/NWN/' 
      end
    else
      begin
        S := NWNlocation;
        StringChangeEx(S, '\', '/', True);
      end;

    Result := S;
end;


[INI]
; the ini file is appended based on the location of NWN in the registry

;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "NAME"; String: "Neverwinter Nights"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "API_DICTIONARY_PATH"; String: "resources/apidictionary.xml"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "LANGUAGE_DICTIONARY_PATH"; String: "resources/languageDictionary.xml"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "GAME_MODULE_PATH"; String: "io/ErfFile.class"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "INCLUDES_PATH"; String: "resources/includes"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "SUPPORTED_FILE_EXTENSIONS"; String: "mod"
Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "COMPILER_PATH"; String: "{code:ReplaceNWNSlash}/utils/clcompile.exe"
Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "GAME_DIRECTORY"; String: "{code:ReplaceNWNSlash}"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "ICON_PATH"; String: "resources/NWNIcon_Small.gif"
;Filename: "{app}\translators\nwn\translator.ini"; Section: "settings"; Key: "SUPPORTS_TESTING"; String: "true"

[UninstallDelete]
Type: files; Name: "{app}\user_preferences.ini"
Type: files; Name: "{app}\errors.log"
Type: filesandordirs; Name: "{app}\patterns"
