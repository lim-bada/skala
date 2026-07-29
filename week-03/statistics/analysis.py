# pandas: 표 형태로 데이터를 다루는 라이브러리
import pandas as pd # numpy: 수치 계산을 위한 라이브러리
import numpy as np # matplotlib: 그래프를 그리는 기본 라이브러리
import matplotlib.pyplot as plt # seaborn: 통계 시각화 라이브러리 (matplotlib 기반, 더 예쁜 그래프)
import seaborn as sns # statsmodels: 회귀분석 등 통계 모델을 위한 라이브러리
import statsmodels.api as sm # sklearn: 모델 성능 평가 지표(RMSE, MAE)를 제공
from sklearn.metrics import mean_squared_error, mean_absolute_error
import warnings
warnings.filterwarnings('ignore')

# CSV 파일을 불러와서 데이터프레임(df)에 저장
df = pd.read_csv('data/ev_battery_data.csv')

# shape: 데이터가 몇 행(샘플) x 몇 열(변수)인지 출력
print('=== 데이터 크기 (shape) ===')
print(f'{df.shape[0]}행 x {df.shape[1]}열')

# info(): 각 컬럼의 데이터 타입(int/float/str)과 null이 아닌 값의 개수 확인
print('\n=== 컬럼 정보 (info) ===')
df.info()

# describe(): 숫자형 컬럼의 기초 통계량(평균, 표준편차, 최소/최대 등) 출력
print('\n=== 기초 통계량 (describe) ===')
print(df.describe())

# isnull().sum(): 컬럼별 결측치(빈 값) 개수 확인 — 0이면 깨끗한 데이터
print('\n=== 결측치 확인 (isnull) ===')
print(df.isnull().sum())

# 6. 히스토그램으로 분포 시각화
# subplots: 2행 2열로 4개 그래프를 한 번에 그림
fig, axes = plt.subplots(2, 2, figsize=(12, 10))

# 히스토그램을 그릴 4개 변수 목록
columns = ['Battery_Price', 'Range_km', 'Marketing_Spend', 'EV_Sales']

# 각 그래프의 한글 제목 (학습자가 이해하기 쉽게)
titles = ['배터리 가격 분포 (만원)', '주행거리 분포 (km)', '마케팅비 분포 (만원)', '전기차 판매량 분포 (대)']
# ============================================================
# TODO: 4개 변수의 히스토그램을 그리고 저장하세요.
# - ax.hist()를 사용하여 각 변수의 분포를 시각화하세요.
# - bins=10, edgecolor='black', alpha=0.7 파라미터를 사용하세요.
# - 각 그래프에 적절한 제목(ax.set_title)과 x축 레이블(ax.set_xlabel)을 설정하세요.
for ax, col, title in zip(axes.flatten(), columns, titles):
    ax.hist(df[col], bins=10, edgecolor="black", alpha=0.7)
    ax.set_title(title)
    ax.set_xlabel(col)
# HINT:
# # 4개 변수를 반복하면서 각각 히스토그램 생성
# for ax, col, title in zip(axes.flatten(), columns, titles):
# # bins=10: 10개 구간으로 나눔, edgecolor: 막대 테두리, alpha: 투명도
# ax.hist(df[col], bins=10, edgecolor='black', alpha=0.7)
# ax.set_title(title)
# ax.set_xlabel(col)
#
# 참고: statistics_example.ipynb 의 Step 2를 확인하세요.
# ============================================================

# 그래프 간격 자동 조정 (레이블 겹침 방지)
plt.tight_layout()
# png 파일로 저장 (plt.show() 전에 저장해야 빈 파일이 안 생김)
plt.savefig('visualizations/hist_all.png')
print('\nhist_all.png 저장 완료')

# 7. 상관 행렬 계산 및 출력
# .corr(): 숫자형 컬럼 간 상관계수(-1~1)를 계산하여 행렬로 반환
print('\n=== 상관 행렬 ===')
corr_matrix = df[['Battery_Price', 'Range_km', 'Marketing_Spend', 'EV_Sales']].corr()
print(corr_matrix.round(3))

# ============================================================
# TODO: sns.heatmap()을 사용하여 상관 행렬을 히트맵으로 시각화하고 저장하세요.
# - plt.figure(figsize=(8, 6)) 로 그림 크기를 설정하세요.
# - sns.heatmap()에 annot=True, cmap='RdBu_r', center=0,
# square=True, fmt='.3f' 파라미터를 사용하세요.
# - 제목은 '변수 간 상관 행렬 히트맵'으로 설정하세요.
# - visualizations/heatmap.png 로 저장하세요.
plt.figure(figsize=(8, 6))
sns.heatmap(corr_matrix, annot=True, cmap='RdBu_r', center=0, 
            square=True, fmt='.3f')
plt.title('변수 간 상관 행렬 히트맵')
plt.tight_layout()
plt.savefig('visualizations/heatmap.png')
# print("heatmap.png 저장 완료")
# HINT:
# # heatmap: 상관 행렬을 색깔로 시각화 (빨강=양의 상관, 파랑=음의 상관)
# plt.figure(figsize=(8, 6))
# # annot=True: 셀 안에 숫자 표시, cmap: 컬러맵, center=0: 0 기준 대칭
# # square=True: 정사각형 셀, fmt: 숫자 형식
# sns.heatmap(corr_matrix, annot=True, cmap='RdBu_r', center=0,
# square=True, fmt='.3f')
# plt.title('변수 간 상관 행렬 히트맵')
# plt.tight_layout()
# plt.savefig('visualizations/heatmap.png')
#
# 참고: statistics_example.ipynb 의 Step 3을 확인하세요.
# ============================================================

# 9. EV_Sales와 가장 상관 높은 변수 찾기
# drop('EV_Sales'): 자기 자신(EV_Sales vs EV_Sales = 1.0)은 제외
ev_corr = corr_matrix['EV_Sales'].drop('EV_Sales')
# abs().idxmax(): 절대값이 가장 큰 변수명 찾기 (음의 상관도 고려)
strongest_var = ev_corr.abs().idxmax()
strongest_val = ev_corr[strongest_var]
print(f'\nEV_Sales와 가장 상관이 높은 변수: {strongest_var} ({strongest_val:.4f})')

# ============================================================
# TODO: sns.pairplot()을 사용하여 산점도 행렬을 그리고 저장하세요.
# - 4개 숫자형 변수(Battery_Price, Range_km, Marketing_Spend, EV_Sales)를 선택하세요.
# - visualizations/scatter_matrix.png 로 저장하세요.
g = sns.pairplot(df[['Battery_Price', 'Range_km', 'Marketing_Spend', 'EV_Sales']])
#plt.show()
g.savefig('visualizations/scatter_matrix.png')
print('\nscatter_matrix.png 저장 완료')
# HINT:
# # pairplot: 모든 변수 쌍의 산점도를 한 번에 그림
# # 대각선: 각 변수의 히스토그램, 비대각선: 두 변수의 관계(직선/곡선/무관계)
# g = sns.pairplot(df[['Battery_Price', 'Range_km', 'Marketing_Spend', 'EV_Sales']])
# # plt.show() 전에 저장해야 빈 파일이 안 생김
# g.savefig('visualizations/scatter_matrix.png')
# print('scatter_matrix.png 저장 완료')
#
# 참고: statistics_example.ipynb 의 Step 4를 확인하세요.
# ============================================================

# 10. 단순 선형 회귀 (Range_km만 사용)
# X(독립변수): 주행거리 하나만 사용 — 단순 선형 회귀
X_simple = df[['Range_km']]
# Y(종속변수): 예측하고자 하는 대상 = 전기차 판매량
y = df['EV_Sales']
# ============================================================
# TODO: 상수항을 추가하고 OLS 회귀 모델을 학습하세요.
# - sm.add_constant()로 X_simple에 상수항을 추가하세요.
# - sm.OLS(y, X_simple_const).fit() 으로 모델을 학습하세요.
X_simple_const = sm.add_constant(X_simple)
model_simple = sm.OLS(y, X_simple_const).fit()
# HINT:
# # add_constant: 상수항(절편) 추가 — 직선이 원점(0,0)을 지나지 않아도 됨
# X_simple_const = sm.add_constant(X_simple)
# # OLS(최소자승법): 실제값과 예측값 차이의 제곱합을 최소화하는 직선 탐색
# model_simple = sm.OLS(y, X_simple_const).fit()
#
# 참고: statistics_example.ipynb 의 Step 5를 확인하세요.
# ============================================================
print('\n========== 단순 선형 회귀 결과 (Range_km만 사용) ==========')
# summary(): R², p-value, 계수 등 모든 결과를 한 번에 출력
print(model_simple.summary())
# rsquared: 결정계수 — 모델이 데이터를 얼마나 설명하는지 (0~1)
print(f'\n단순 회귀 R-squared: {model_simple.rsquared:.4f}')

# 11. 다중 선형 회귀 (모든 변수 사용)
# X에 3개 변수(Battery_Price, Range_km, Marketing_Spend)를 모두 사용
X_multi = df[['Battery_Price', 'Range_km', 'Marketing_Spend']]
# ============================================================
# TODO: 상수항을 추가하고 OLS 다중 회귀 모델을 학습하세요.
# - Step 5와 동일한 패턴입니다: add_constant → OLS → fit
# - model_multi 변수에 결과를 저장하세요.
X_multi_const = sm.add_constant(X_multi)
model_multi = sm.OLS(y, X_multi_const).fit()
# HINT:
# # add_constant: 상수항(절편) 추가 — 원점을 지나지 않아도 되는 자유도 확보
# X_multi_const = sm.add_constant(X_multi)
# # OLS.fit(): 최소자승법으로 최적의 회귀 계수 탐색
# model_multi = sm.OLS(y, X_multi_const).fit()
#
# 참고: statistics_example.ipynb 의 Step 6을 확인하세요.
# ============================================================
print('\n========== 다중 선형 회귀 결과 (모든 변수 사용) ==========')
# summary(): R², Adj.R², 각 변수의 계수와 p-value를 한 번에 출력
print(model_multi.summary())
# 단순 회귀 vs 다중 회귀 R² 비교 — 변수 추가로 설명력이 얼마나 향상되었나?
print(f'\n다중 회귀 R-squared: {model_multi.rsquared:.4f}')
print(f'단순 회귀 R-squared: {model_simple.rsquared:.4f}')
print(f'R-squared 향상: {model_multi.rsquared - model_simple.rsquared:.4f}')

# 12. 주요 수치 출력
# rsquared: 결정계수 — 모델이 데이터를 얼마나 설명하는지 (0~1, 클수록 좋음)
# rsquared_adj: 변수 개수를 고려하여 보정한 R² (다중 회귀에서 더 신뢰할 만함)
# params[]: 회귀 계수 — X가 1단위 변할 때 Y의 변화량
# pvalues[]: 유의확률 — 0.05 미만이면 통계적으로 유의미
print('\n========== 주요 수치 요약 ==========')
print(f'단순 회귀 (Range_km만)')
print(f' R-squared: {model_simple.rsquared:.4f}')
print(f' Adj. R-squared: {model_simple.rsquared_adj:.4f}')
print(f' Range_km coefficient: {model_simple.params["Range_km"]:.4f}')
print(f' Range_km p-value: {model_simple.pvalues["Range_km"]:.6f}')
print(f'\n다중 회귀 (Battery_Price + Range_km + Marketing_Spend)')
print(f' R-squared: {model_multi.rsquared:.4f}')
print(f' Adj. R-squared: {model_multi.rsquared_adj:.4f}')
print(f' Battery_Price coefficient: {model_multi.params["Battery_Price"]:.4f}')
print(f' Battery_Price p-value: {model_multi.pvalues["Battery_Price"]:.6f}')
print(f' Range_km coefficient: {model_multi.params["Range_km"]:.4f}')
print(f' Range_km p-value: {model_multi.pvalues["Range_km"]:.6f}')
print(f' Marketing_Spend coefficient: {model_multi.params["Marketing_Spend"]:.4f}')
print(f' Marketing_Spend p-value: {model_multi.pvalues["Marketing_Spend"]:.6f}')
# 13. RMSE, MAE 계산 (모델 성능 평가)
# predict(): 학습된 모델로 기존 데이터의 예측값 계산
y_pred_simple = model_simple.predict(X_simple_const)
y_pred_multi = model_multi.predict(X_multi_const)
# RMSE: 예측 오차 제곱의 평균에 루트 — 큰 오차에 더 민감
rmse_simple = np.sqrt(mean_squared_error(y, y_pred_simple))
# MAE: 예측 오차 절대값의 평균 — 직관적 이해 쉬움
mae_simple = mean_absolute_error(y, y_pred_simple)
rmse_multi = np.sqrt(mean_squared_error(y, y_pred_multi))
mae_multi = mean_absolute_error(y, y_pred_multi)
print(f'\n--- 모델 성능 비교 ---')
print(f'단순 회귀 RMSE: {rmse_simple:.2f}, MAE: {mae_simple:.2f}')
print(f'다중 회귀 RMSE: {rmse_multi:.2f}, MAE: {mae_multi:.2f}')
# ============================================================
# TODO: model_summary.txt 파일에 단순 회귀와 다중 회귀의
# 주요 수치를 저장하세요.
#
# 포함해야 할 내용 (단순 회귀):
# - R-squared, Adj. R-squared, RMSE, MAE
# - Range_km coefficient, Range_km p-value
#
# 포함해야 할 내용 (다중 회귀):
# - R-squared, Adj. R-squared, RMSE, MAE
# - Battery_Price coefficient, Battery_Price p-value
# - Range_km coefficient, Range_km p-value
# - Marketing_Spend coefficient, Marketing_Spend p-value
with open('model_summary.txt', 'w', encoding='utf-8') as f:
    f.write('=== 단순 선형 회귀 (Range_km만) ===\n')
    f.write(f'R-squared: {model_simple.rsquared:.4f}\n')
    f.write(f"Adj. R-squared: {model_simple.rsquared_adj:.4f}\n")
    f.write(f"RMSE: {rmse_simple:.2f}\n")
    f.write(f"MAE: {mae_simple:.2f}\n")
    f.write(f'Range_km coefficient: {model_simple.params["Range_km"]:.4f}\n')
    f.write(f'Range_km p-value: {model_simple.pvalues["Range_km"]:.6f}\n\n')
    f.write("=== 다중 선형 회귀 (모든 변수) ===\n")
    f.write(f"R-squared: {model_multi.rsquared:.4f}\n")
    f.write(f"Adj. R-squared: {model_multi.rsquared_adj:.4f}\n")
    f.write(f"RMSE: {rmse_multi:.2f}\n")
    f.write(f"MAE: {mae_multi:.2f}\n")
    f.write(f'Battery_Price coefficient: {model_multi.params["Battery_Price"]:.4f}\n')
    f.write(f'Battery_Price p-value: {model_multi.pvalues["Battery_Price"]:.6f}\n')
    f.write(f'Range_km coefficient: {model_multi.params["Range_km"]:.4f}\n')
    f.write(f'Range_km p-value: {model_multi.pvalues["Range_km"]:.6f}\n')
    f.write(f'Marketing_Spend coefficient: {model_multi.params["Marketing_Spend"]:.4f}\n')
    f.write(f'Marketing_Spend p-value: {model_multi.pvalues["Marketing_Spend"]:.6f}\n')
print("\nmodel_summary.txt 저장 완료")
# HINT:
# # 텍스트 파일로 결과 저장 (제출 및 채점용)
# with open('model_summary.txt', 'w', encoding='utf-8') as f:
# f.write('=== 단순 선형 회귀 (Range_km만) ===\n')
# f.write(f'R-squared: {model_simple.rsquared:.4f}\n')
# ...
#
# 참고: statistics_example.ipynb 의 Step 7을 확인하세요.
# ============================================================

# ============================================================
# TODO: 미래 시나리오 예측을 수행하세요.
#
# 조건:
# - Battery_Price = 1000 (만원)
# - Range_km = 500 (km)
# - Marketing_Spend = 5000 (만원)
#
# 주의: predict()에 전달하는 DataFrame의 컬럼명은 모델 학습 시
# 사용한 X_multi_const의 컬럼명과 정확히 일치해야 합니다.
# (const, Battery_Price, Range_km, Marketing_Spend)
scenario = pd.DataFrame({
    'const': [1], # 상수항 (add_constant로 추가한 절편)
    'Battery_Price': [1000], # 배터리 가격 1000만원
    'Range_km': [500], # 주행거리 500km
    'Marketing_Spend': [5000] # 마케팅비 5000만원
})

# predict(): 학습된 모델로 새로운 조건의 판매량 예측
predicted_sales = model_multi.predict(scenario)
print(f'예측 EV_Sales: {predicted_sales.values[0]:.0f}대')

# prediction.txt 에 예측값(숫자만)을 저장하세요.
with open('prediction.txt', 'w', encoding='utf-8') as f:
    f.write(f'{predicted_sales.values[0]:.0f}')
print("\nprediction.txt 저장 완료")
# HINT:
# # 예측 시나리오: 컬럼명과 순서는 학습 시 X_multi_const와 동일해야 함
# scenario = pd.DataFrame({
# 'const': [1], # 상수항 (add_constant로 추가한 절편)
# 'Battery_Price': [1000], # 배터리 가격 1000만원
# 'Range_km': [500], # 주행거리 500km
# 'Marketing_Spend': [5000] # 마케팅비 5000만원
# })
# # predict(): 학습된 모델로 새로운 조건의 판매량 예측
# predicted_sales = model_multi.predict(scenario)
# print(f'예측 EV_Sales: {predicted_sales.values[0]:.0f}대')
#
# prediction.txt 에 예측값(숫자만)을 저장하세요.
# with open('prediction.txt', 'w', encoding='utf-8') as f:
# f.write(f'{predicted_sales.values[0]:.0f}')
#
# 참고: statistics_example.ipynb 의 Step 8을 확인하세요.
# ============================================================

# 14. 회귀 계수 부호 해석
# params[]: 모델에 저장된 각 변수의 회귀 계수를 불러옴
print('\n========== 회귀 계수 해석 ==========')
coef_bp = model_multi.params['Battery_Price'] # Battery_Price의 계수
coef_rk = model_multi.params['Range_km'] # Range_km의 계수
coef_ms = model_multi.params['Marketing_Spend'] # Marketing_Spend의 계수
# ============================================================
# TODO: 각 계수의 부호를 확인하고 비즈니스 인사이트를 출력하는
# 조건문을 작성하세요.
#
# - Battery_Price: 음수면 "가격이 낮을수록 판매량 증가"
# 양수면 "가격이 높을수록 판매량 증가"
# - Range_km: 양수면 "주행거리가 길수록 판매량 증가"
# 음수면 "주행거리가 짧을수록 판매량 증가"
# - Marketing_Spend: 양수면 "마케팅 투자가 많을수록 판매량 증가"
# 음수면 "마케팅 투자가 적을수록 판매량 증가"
if coef_bp < 0:
    print(f'Battery_Price 계수 = {coef_bp:.4f} (음수) → 배터리 가격이 낮을수록 판매량이 증가합니다.')
else:
    print(f'Battery_Price 계수 = {coef_bp:.4f} (양수) → 배터리 가격이 높을수록 판매량이 증가합니다.')

if coef_rk > 0:
      print(f'Range_km 계수 = {coef_rk:.4f} (양수) → 주행거리가 길수록 판매량이 증가합니다.')
else:
      print(f'Range_km 계수 = {coef_rk:.4f} (음수) → 주행거리가 짧을수록 판매량이 증가합니다.')

if coef_ms > 0:
      print(f'Marketing_Spend 계수 = {coef_ms:.4f} (양수) → 마케팅 투자가 많을수록 판매량이 증가합니다.')
else:
      print(f'Marketing_Spend 계수 = {coef_ms:.4f} (음수) → 마케팅 투자가 적을수록 판매량이 증가합니다.')

# HINT:
# # 계수가 음수이면 반대 방향, 양수이면 같은 방향
# if coef_bp < 0:
# print(f'Battery_Price 계수 = {coef_bp:.4f} (음수) → ...')
# else:
# print(f'Battery_Price 계수 = {coef_bp:.4f} (양수) → ...')
#
# 참고: statistics_example.ipynb 의 Step 9를 확인하세요.
# ============================================================

# 15. 표준화 계수 계산 (실질적 영향력 비교)
# 표준화 계수 = 회귀 계수 x 표준편차 — 단위가 다른 변수들을 공정하게 비교하는 방법
print('\n========== 표준화 계수 (실질적 영향력) ==========')
# std(): 해당 변수의 표준편차 — 데이터가 평균에서 얼마나 퍼져 있는지 나타냄
std_bp = df['Battery_Price'].std()
std_rk = df['Range_km'].std()
std_ms = df['Marketing_Spend'].std()

# 표준화 계수 계산: "변수가 1표준편차 변할 때 판매량이 몇 대 변하는가?"
std_coef_bp = coef_bp * std_bp
std_coef_rk = coef_rk * std_rk
std_coef_ms = coef_ms * std_ms

print(f'Battery_Price: 계수 {coef_bp:.4f} × 표준편차 {std_bp:.1f} = {std_coef_bp:.1f}')
print(f' → 배터리 가격이 1표준편차({std_bp:.0f}만원) 변할 때 판매량이 {abs(std_coef_bp):.0f}대 변함')
print(f'Range_km: 계수 {coef_rk:.4f} × 표준편차 {std_rk:.1f} = {std_coef_rk:.1f}')
print(f' → 주행거리가 1표준편차({std_rk:.0f}km) 변할 때 판매량이 {abs(std_coef_rk):.0f}대 변함')
print(f'Marketing_Spend: 계수 {coef_ms:.4f} × 표준편차 {std_ms:.1f} = {std_coef_ms:.1f}')
print(f' → 마케팅비가 1표준편차({std_ms:.0f}만원) 변할 때 판매량이 {abs(std_coef_ms):.0f}대 변함')
# ============================================================
# TODO: 표준화 계수를 절대값 기준으로 내림차순 정렬하여 출력하고,
# 어떤 변수가 판매량에 가장 큰 영향을 미치는지 결론을 출력하세요.
std_coefs = {
      'Battery_Price': abs(std_coef_bp),
      'Range_km': abs(std_coef_rk),
      'Marketing_Spend': abs(std_coef_ms)
  }
print('\n표준화 계수 비교 (절대값 기준):')

for var, val in sorted(
      std_coefs.items(),
      key=lambda x: x[1],
      reverse=True
  ):
      print(f'{var}: {val:.1f}')

most_influential = max(std_coefs, key=std_coefs.get)
print(f'결론: {most_influential}의 표준화 계수가 가장 큽니다.')

# HINT:
# # 딕셔너리에 변수명과 절대값 저장
# std_coefs = {'Battery_Price': abs(std_coef_bp), ...}
# # sorted: 표준화 계수(값) 기준 내림차순 정렬 (reverse=True)
# for var, val in sorted(std_coefs.items(), key=lambda x: x[1], reverse=True):
# print(f' {var}: {val:.1f}')
# # max: 딕셔너리에서 가장 큰 값을 가진 키(변수명) 찾기
# print(f'결론: ...의 표준화 계수가 가장 큽니다. ...')
#
# 참고: statistics_example.ipynb 의 Step 10을 확인하세요.
# ============================================================
