package com.example.multidb.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建商品请求")
public class CreateProductRequest {

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "Spring Boot 实战")
    private String name;

    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", example = "Spring Boot 4 入门与进阶")
    private String description;

    @NotBlank(message = "分类不能为空")
    @Schema(description = "分类", example = "BOOK")
    private String category;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于 0")
    @Schema(description = "价格", example = "89.9")
    private Double price;
}
