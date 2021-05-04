package app.itetenosuke.api.application.medicine;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.itetenosuke.api.domain.medicine.IMedicineRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MedicineUseCase {
  private IMedicineRepository medicineRepository;

  @Transactional(readOnly = true)
  public List<MedicineDto> getMedicineList(String userId) {
    return medicineRepository
        .findAllByUserId(userId)
        .stream()
        .map(
            v -> {
              return MedicineDto.builder()
                  .medicineId(v.getMedicineId())
                  .medicineName(v.getMedicineName())
                  .status(v.getStatus())
                  .createdAt(v.getCreatedAt())
                  .updatedAt(v.getUpdatedAt())
                  .build();
            })
        .collect(Collectors.toList());
  }
}
