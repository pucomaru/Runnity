#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
JMeter용 테스트 회원 CSV 파일 생성 스크립트
각 PC별로 다른 회원 범위를 사용하도록 파일을 분리하여 생성
"""

def generate_csv_files(start_member_id=13, challenge_id=38, members_per_pc=500, num_files=10):
    """
    테스트 회원 CSV 파일 생성
    
    Args:
        start_member_id: 시작 member_id (기본값: 13)
        challenge_id: 챌린지 ID (기본값: 38)
        members_per_pc: PC당 사용할 회원 수 (기본값: 500)
        num_files: 생성할 파일 개수 (기본값: 10)
    """
    
    for file_num in range(1, num_files + 1):
        filename = f'test_members_{file_num}.csv'
        
        # 각 PC별 member_id 범위 계산
        start_id = start_member_id + (file_num - 1) * members_per_pc
        end_id = start_id + members_per_pc - 1
        
        with open(filename, 'w', encoding='utf-8') as f:
            # CSV 헤더
            f.write('memberId,challengeId\n')
            
            # 각 member_id에 대해 challenge_id와 함께 작성
            for member_id in range(start_id, end_id + 1):
                f.write(f'{member_id},{challenge_id}\n')
        
        print(f"✅ {filename} 생성 완료: member_id {start_id}~{end_id} ({members_per_pc}명)")
    
    print(f"\n📝 총 {num_files}개 파일 생성 완료")
    print(f"   각 파일당 {members_per_pc}명, 총 {num_files * members_per_pc}명")
    print(f"\n📋 파일별 범위:")
    for file_num in range(1, num_files + 1):
        start_id = start_member_id + (file_num - 1) * members_per_pc
        end_id = start_id + members_per_pc - 1
        print(f"   test_members_{file_num}.csv: {start_id}~{end_id}")

if __name__ == '__main__':
    generate_csv_files(
        start_member_id=13,
        challenge_id=38,
        members_per_pc=500,  # PC당 500명
        num_files=10          # 10개 파일 (PC 10대)
    )

