### 문제 요약
GitHub Actions 빌드 시 YAML 파일의 쉘 스크립트 문법 에러(`syntax error near unexpected token '('`)로 빌드 실패 | **타입**: CI/CD | **환경**: GitHub Actions (Ubuntu Runner)

### 원인 분석
**근본 원인**: `SEJONG-MALSAMI-BE-CICD.yaml`의 일부 스텝에서 GitHub Secrets의 값을 환경변수 매핑 없이 `run` 문맥 안에 직접 `${{ secrets.XXX }}` 형태로 주입하여 `echo "${{ secrets.XXX }}"`를 실행했습니다.

**발생 메커니즘**:
1. 특정 비밀값(예: `APPLICATION_PROD_YML` 등의 파일 내용) 내부에 괄호 `(` 및 `)` 또는 특수 문자가 포함되어 있었습니다.
2. GitHub Actions가 스크립트 실행을 위한 임시 쉘 파일(`.sh`)을 작성할 때, 해당 비밀값 텍스트를 그대로 하드코딩 치환했습니다.
3. 쉘 해석기(bash)가 이를 실행하는 과정에서 괄호 `(`를 만나자 올바르지 않은 쉘 구문으로 해석하여 `syntax error near unexpected token '('` 에러를 유발하며 프로세스가 즉시 실패했습니다.

### 해결 방법
#### Root Fix (권장)
GitHub Actions 공식 보안 및 문법 권장 가이드에 따라, `secrets` 값을 `run` 쉘 스크립트 문자열에 직접 주입하지 않고, `env:` 블록을 통해 쉘 환경 변수로 주입합니다. 그런 다음 쉘 내부에서는 안전하게 변수 `"$VAR_NAME"` 형식을 사용하여 출력합니다.
이 방식은 값에 어떠한 특수 문자(괄호, 쌍따옴표, $, ` 등)가 있더라도 쉘 구문 에러를 100% 방지하고 로그 및 프로세스 유출 위험도 완벽히 막아줍니다.

### 검증
1. `.github/workflows/SEJONG-MALSAMI-BE-CICD.yaml` 수정 반영 후 push하여 GitHub Actions 빌드 통과 여부 확인

### 재발 방지
- GitHub Actions의 `run` 블록 내에서 `${{ secrets.XXX }}`를 직접 호출하는 방식 지양
- 항상 `env:` 블록을 거쳐 환경 변수로 사용하도록 표준 규격 수립
