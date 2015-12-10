; ScriptEase I installer script
; @author Richard Zhao

[Setup]
AppName=ScriptEase
AppVerName=ScriptEase
DefaultDirName={pf}\ScriptEase
; Since no icons will be created in "{group}", we don't need the wizard
; to ask for a Start Menu folder name:
DisableProgramGroupPage=yes
UninstallDisplayIcon={app}\nwnse1.ico
OutputDir=.

[Files]
; all files should be listed here
Source: "ScriptEase.jar"; DestDir: "{app}";
Source: "ScriptEase.url"; DestDir: "{app}";
Source: "nwnse1.ico"; DestDir: "{app}";

[Icons]
; all shortcuts should be created here
Name: "{commonprograms}\ScriptEase\ScriptEase"; Filename: "{pf}\Java\jre1.6.0_07\bin\javaw.exe"; Parameters: "-Xmx800M -jar ""{app}\ScriptEase.jar"""; WorkingDir: "{app}"; IconFilename: "{app}\nwnse1.ico"
Name: "{commondesktop}\ScriptEase"; Filename: "{pf}\Java\jre1.6.0_07\bin\javaw.exe"; Parameters: "-Xmx800M -jar ""{app}\ScriptEase.jar"""; WorkingDir: "{app}"; IconFilename: "{app}\nwnse1.ico"
Name: "{commonprograms}\ScriptEase\ScriptEase on the Web"; Filename: "{app}\ScriptEase.url";

[code]

var
    NWNlocation: String;
    
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

[INI]
; the ini file is generated based on the location of NWN in the registry
Filename: "{app}\scriptease.ini"; Section: "Locations"; Flags: uninsdeletesection; Key: "NWN"; String: "{reg:HKLM\SOFTWARE\Bioware\NWN\Neverwinter,Location|C:\NeverwinterNights\NWN}"

[UninstallDelete]
Type: files; Name: "{app}\scriptease.ini"

