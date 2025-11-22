; Script Inno Setup para Diagonal Cordéis
; Requer Inno Setup 6.0 ou superior
; Download: https://jrsoftware.org/isdl.php

#define MyAppName "Diagonal Cordéis"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Diagonal"
#define MyAppExeName "DiagonalCordeis.bat"
#define MyAppJarName "cordeis-0.0.1-SNAPSHOT.jar"

[Setup]
; Informações do aplicativo
AppId={{F4B8C2A1-8D3E-4F5B-9A7C-1E2D3F4A5B6C}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL=https://github.com/diagonal-cordeis
AppSupportURL=https://github.com/diagonal-cordeis
DefaultDirName={autopf}\DiagonalCordeis
DisableProgramGroupPage=yes
; Ícone (remover linha se não tiver ícone)
; SetupIconFile=src\main\resources\images\icon.ico
OutputDir=target\installer
OutputBaseFilename=DiagonalCordeis-Setup-{#MyAppVersion}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

; Requer Java 21 ou superior
MinVersion=10.0

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Arquivos da aplicação
Source: "target\installer-files\{#MyAppJarName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\installer-files\DiagonalCordeis.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\installer-files\init-db.sql"; DestDir: "{app}"; Flags: ignoreversion skipifsourcedoesntexist
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
function InitializeSetup(): Boolean;
var
  ResultCode: Integer;
  JavaVersion: String;
begin
  Result := True;
  
  // Verificar se Java está instalado
  if Exec('java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    // Java encontrado
    Result := True;
  end
  else
  begin
    // Java não encontrado
    if MsgBox('Java 21 ou superior não foi encontrado no sistema.' + #13#10 + 
              'O programa precisa do Java para funcionar.' + #13#10#13#10 +
              'Deseja continuar mesmo assim? (Você precisará instalar o Java depois)', 
              mbConfirmation, MB_YESNO) = IDYES then
      Result := True
    else
      Result := False;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  AppDataPath: String;
begin
  if CurStep = ssPostInstall then
  begin
    // Criar diretório de dados em %APPDATA%
    AppDataPath := ExpandConstant('{userappdata}\DiagonalCordeis\data');
    if not DirExists(AppDataPath) then
      CreateDir(AppDataPath);
  end;
end;

[UninstallDelete]
; Não deletar dados do usuário por segurança
; Type: filesandordirs; Name: "{userappdata}\DiagonalCordeis"

[Messages]
brazilianportuguese.WelcomeLabel2=Este assistente irá instalar o [name/ver] no seu computador.%n%nO banco de dados será criado automaticamente em:%n%n%APPDATA%\DiagonalCordeis\data\
