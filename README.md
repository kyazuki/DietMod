# DietMod
Let's diet yourself!

## 対応バージョン
Minecraft: 1.16.x<br>
Minecraft Forge: 32.0.0以上<br>
http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.16.1.html

## 概説
プレイヤーが太ります。<br>
歩いた距離に応じて徐々に痩せていきますが、食べ物を食べるとその回復量に応じて太ります。<br>
リスポーン時にウエストが最大値にリセットされます。<br>
初期状態以上に太ることはありません。<br>
太っているほど最大体力値が増加し、当たり判定が拡大し、採掘速度が上昇し、移動速度が減少します。<br>
痩せているほど最大体力値が減少し、当たり判定が縮小し、採掘速度が減少し、移動速度が上昇し、跳躍力が上昇します。<br>
痩せすぎると死にます。

## コマンド
- /setdistance <ブロック数>
  - 通常体型に戻るまでに必要な移動距離(単位:ブロック)です。<br>
    configファイルにおけるdistanceToNormalを一時的に変更するコマンドです。

## configファイル
Modを導入した状態で一回でも起動すると、configフォルダ内にdietmod-common.tomlが生成されます。<br>
値の変更は次回の起動時に適用されます。
- maxScale
  - プレイヤーの最大サイズです。通常サイズの何倍かで指定します。<br>
    1.0〜10.0の間で指定でき、デフォルトは2.0です。
- distanceToNormal
  - 通常体型に戻るまでに必要な移動距離(単位:ブロック)です。<br>
    (※途中で食べ物を食べるとその分太るので、設定した距離とはズレます。)<br>
    100～100000の間で指定でき、デフォルトは1000です。動画の尺に合わせて調整してください。
- killHealth
  - 最大体力値がこの値を下回ると強制的に死にます。<br>
    0.1〜2.0の間で指定でき、デフォルトは0.45です。0.1でハート1個分にあたります。
- count_food
  - 食べ物を食べると太るかどうか。<br>
    trueかfalseで指定でき、デフォルトはtrueです。
- food_modifier
  - 食べ物を食べたときにどれくらい太るか。各食べ物の回復値にこれを掛け合わせた値を割合として太ります。<br>
    例えばステーキの場合、この値が0.025なら、回復値8×0.025=0.2なので、通常ウエストを基準として20%太ります。<br>
    0.0〜10.0の間で指定でき、デフォルトは0.025です。
- change_hitbox
  - プレイヤーの当たり判定を変更するかどうか。<br>
    trueかfalseで指定でき、デフォルトはtrueです。
- change_max_health
  - プレイヤーの最大体力値を変更するかどうか。<br>
    trueかfalseで指定でき、デフォルトはtrueです。
- change_speed
  - プレイヤーの移動速度を変更するかどうか。<br>
    trueかfalseで指定でき、デフォルトはtrueです。
- change_jump_boost
  - プレイヤーの跳躍力を変更するかどうか。<br>
    trueかfalseで指定でき、デフォルトはtrueです。

## 注意点
- プレイヤーを見るとき、側面からの視点では太っているように見えますが、上下からの視点だと通常比率に見えてしまいます。
  - Forgeの仕様上の限界です…全方位に対応するにはかなり手を加えなければならず、<br>
    参考動画でも同じ挙動をしている様子だったので、断念しました。
- 体力について、通常の体力値(ハート10個分)を超える分はログアウト時に破棄されます。<br>
  ウエストや体力上限値は保持されます。
