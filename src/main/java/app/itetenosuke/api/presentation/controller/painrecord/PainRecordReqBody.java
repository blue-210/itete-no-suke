package app.itetenosuke.api.presentation.controller.painrecord;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import app.itetenosuke.api.domain.bodypart.BodyPart;
import app.itetenosuke.api.domain.medicine.Medicine;
import lombok.Data;

@Data
public class PainRecordReqBody {
  private String painRecordId;
  // TODO Value Objectに変更
  private String userId;
  // 痛みレベル
  @NotNull
  @Range(min = 0, max = 3)
  private Integer painLevel;

  // お薬
  @Valid private List<Medicine> medicineList;

  // 部位
  private List<BodyPart> bodyPartsList;

  // 部位画像
  // private List<Image> imageList;

  // メモ
  @Length(min = 0, max = 250)
  private String memo;

  @DateTimeFormat(pattern = "yyyy年M月d日H時m分")
  private LocalDateTime createdAt;

  @DateTimeFormat(pattern = "yyyy年M月d日H時m分")
  private LocalDateTime updatedAt;
}
