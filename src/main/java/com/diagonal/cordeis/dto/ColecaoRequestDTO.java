package com.diagonal.cordeis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColecaoRequestDTO {
    private String nome;
    private String sku;
    private List<Long> idsLivros;
}
