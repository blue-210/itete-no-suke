package app.itetenosuke.api.infra.db.bodypart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import app.itetenosuke.api.domain.bodypart.BodyPart;
import app.itetenosuke.api.domain.bodypart.IBodyPartRepository;
import app.itetenosuke.api.domain.painrecord.PainRecord;
import app.itetenosuke.api.domain.shared.Status;
import app.itetenosuke.infra.db.jooq.generated.tables.BODY_PARTS_ENROLLMENTS_TABLE;
import app.itetenosuke.infra.db.jooq.generated.tables.BODY_PARTS_TABLE;
import app.itetenosuke.infra.db.jooq.generated.tables.PAIN_RECORDS_TABLE;
import app.itetenosuke.infra.db.jooq.generated.tables.records.BodyPartsRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@AllArgsConstructor
@Slf4j
public class BodyPartRepositoryImpl implements IBodyPartRepository {
  private final DSLContext create;
  private static final BODY_PARTS_TABLE B = BODY_PARTS_TABLE.BODY_PARTS.as("B");
  private static final BODY_PARTS_ENROLLMENTS_TABLE BE =
      BODY_PARTS_ENROLLMENTS_TABLE.BODY_PARTS_ENROLLMENTS.as("BE");
  private static final PAIN_RECORDS_TABLE P = PAIN_RECORDS_TABLE.PAIN_RECORDS.as("P");

  @Override
  @Transactional
  public void save(PainRecord painRecord) {
    // log出力追加
    Integer resultCount = -1;
    try {
      resultCount =
          painRecord
              .getBodyPartsList()
              .stream()
              .mapToInt(
                  bodyPart -> {
                    Integer bodyPartsCount =
                        create
                            .insertInto(B)
                            .set(B.BODY_PART_ID, bodyPart.getBodyPartId())
                            .set(B.USER_ID, bodyPart.getUserId())
                            .set(B.BODY_PART_NAME, bodyPart.getBodyPartName())
                            .set(B.STATUS, bodyPart.getStatus())
                            .set(B.CREATED_AT, bodyPart.getCreatedAt())
                            .set(B.UPDATED_AT, bodyPart.getUpdatedAt())
                            .onDuplicateKeyUpdate()
                            .set(B.BODY_PART_NAME, bodyPart.getBodyPartName())
                            .set(B.STATUS, bodyPart.getStatus())
                            .set(B.UPDATED_AT, bodyPart.getUpdatedAt())
                            .execute();

                    Integer enrollmentCount =
                        create
                            .insertInto(BE)
                            .set(BE.PAIN_RECORD_ID, painRecord.getPainRecordId())
                            .set(BE.BODY_PART_ID, bodyPart.getBodyPartId())
                            .set(BE.BODY_PART_SEQ, bodyPart.getBodyPartSeq())
                            .onDuplicateKeyUpdate()
                            .set(BE.PAIN_RECORD_ID, painRecord.getPainRecordId())
                            .set(BE.BODY_PART_ID, bodyPart.getBodyPartId())
                            .set(BE.BODY_PART_SEQ, bodyPart.getBodyPartSeq())
                            .execute();
                    return bodyPartsCount + enrollmentCount;
                  })
              .sum();
      log.info("BodyPart save count : {}", resultCount);
    } catch (Exception e) {
      log.error("BodyPart save info : {}", painRecord.toString());
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public List<BodyPart> findAllByPainRecordId(String painRecordID) {
    List<Record> selected = new ArrayList<>();
    try {
      selected =
          create
              .select(B.asterisk(), BE.asterisk())
              .from(B)
              .join(BE)
              .on(B.BODY_PART_ID.eq(BE.BODY_PART_ID))
              .join(P)
              .on(P.PAIN_RECORD_ID.eq(BE.PAIN_RECORD_ID))
              .where(P.PAIN_RECORD_ID.eq(painRecordID))
              .orderBy(BE.BODY_PART_SEQ.asc())
              .fetch();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return selected
        .stream()
        .map(
            record -> {
              return BodyPart.builder()
                  .bodyPartId(record.get(B.BODY_PART_ID))
                  .userId(record.get(B.USER_ID))
                  .bodyPartSeq(record.get(BE.BODY_PART_SEQ))
                  .bodyPartName(record.get(B.BODY_PART_NAME))
                  .status(record.get(B.STATUS))
                  .createdAt(record.get(B.CREATED_AT))
                  .updatedAt(record.get(B.UPDATED_AT))
                  .build();
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void delete(String painRecordId) {
    try {
      int result = create.delete(BE).where(BE.PAIN_RECORD_ID.eq(painRecordId)).execute();
      log.info("Delete bodyPart enrollments : count = {}, painRecordId = {}", result, painRecordId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public List<BodyPart> findAllByUserId(String userId) {
    List<Record> selected = new ArrayList<>();
    try {
      selected =
          create
              .select(B.asterisk())
              .from(B)
              .where(B.USER_ID.eq(userId))
              .and(B.STATUS.eq(Status.ALIVE.toString()))
              .orderBy(B.CREATED_AT.desc())
              .fetch();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return selected
        .stream()
        .map(
            record -> {
              return BodyPart.builder()
                  .bodyPartId(record.get(B.BODY_PART_ID))
                  .userId(record.get(B.USER_ID))
                  .bodyPartName(record.get(B.BODY_PART_NAME))
                  .status(record.get(B.STATUS))
                  .createdAt(record.get(B.CREATED_AT))
                  .updatedAt(record.get(B.UPDATED_AT))
                  .build();
            })
        .collect(Collectors.toList());
  }

  @Override
  public Optional<BodyPart> findByBodyPartId(String bodyPartId) {
    Optional<BodyPartsRecord> selected = Optional.empty();
    try {
      selected = create.selectFrom(B).where(B.BODY_PART_ID.eq(bodyPartId)).fetchOptional();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return selected.map(
        v ->
            BodyPart.builder()
                .bodyPartId(v.get(B.BODY_PART_ID))
                .userId(v.get(B.USER_ID))
                .bodyPartName(v.get(B.BODY_PART_NAME))
                .status(v.get(B.STATUS))
                .createdAt(v.get(B.CREATED_AT))
                .updatedAt(v.get(B.UPDATED_AT))
                .build());
  }

  @Override
  public void save(BodyPart bodyPart) {
    try {
      Integer result =
          create
              .insertInto(B)
              .set(B.BODY_PART_ID, bodyPart.getBodyPartId())
              .set(B.USER_ID, bodyPart.getUserId())
              .set(B.BODY_PART_NAME, bodyPart.getBodyPartName())
              .set(B.STATUS, bodyPart.getStatus())
              .set(B.CREATED_AT, bodyPart.getCreatedAt())
              .set(B.UPDATED_AT, bodyPart.getUpdatedAt())
              .onDuplicateKeyUpdate()
              .set(B.BODY_PART_NAME, bodyPart.getBodyPartName())
              .set(B.USER_ID, bodyPart.getUserId())
              .set(B.STATUS, bodyPart.getStatus())
              .set(B.UPDATED_AT, bodyPart.getUpdatedAt())
              .execute();

      log.info("Save bodypart : count = {}, medicine = {}", result, bodyPart.toString());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void deleteByBodyPartId(String bodyPartId) {
    try {
      int result =
          create
              .update(B)
              .set(B.STATUS, Status.DELETED.toString())
              .where(B.BODY_PART_ID.eq(bodyPartId))
              .execute();
      log.info("Delete bodypart : count = {}, painRecordId = {}", result, bodyPartId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
