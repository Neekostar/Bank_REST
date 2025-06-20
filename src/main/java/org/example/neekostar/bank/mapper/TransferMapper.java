package org.example.neekostar.bank.mapper;

import org.example.neekostar.bank.dto.TransferResponseDto;
import org.example.neekostar.bank.entity.Transfer;
import org.example.neekostar.bank.util.CardEncryptor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "fromCardNumber", source = "fromCard.encryptedNumber", qualifiedByName = "decryptAndMask")
    @Mapping(target = "toCardNumber", source = "toCard.encryptedNumber", qualifiedByName = "decryptAndMask")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "timestamp", source = "timestamp")
    TransferResponseDto toDto(Transfer transfer);

    @Named("decryptAndMask")
    default String decryptAndMask(String encrypted) {
        String decrypted = CardEncryptor.decrypt(encrypted);
        return CardEncryptor.maskCardNumber(decrypted);
    }
}
